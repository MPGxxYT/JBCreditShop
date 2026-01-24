package me.mortaldev.jbcreditshop;

import me.mortaldev.jbcreditshop.modules.bundles.BundleManager;
import me.mortaldev.jbcreditshop.modules.playerdata.PlayerDataManager;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Dynamic service registry for dependency injection.
 *
 * <p>Provides a flexible, type-safe service locator pattern for managing dependencies. Services can
 * be registered by class or interface type and retrieved on-demand.
 *
 * <p><b>Example usage:</b>
 *
 * <pre>{@code
 * // Registration (in Main.java onEnable)
 * CrusadersServices services = new CrusadersServices();
 * services.register(ProfileManager.class, profileManager);
 * services.register(IProfileManager.class, profileManager); // By interface
 * services.register(GearBuilder.class, gearBuilder);
 *
 * // Lazy registration (created on first access)
 * services.registerLazy(ExpensiveService.class, () -> new ExpensiveService());
 *
 * // Validation (ensures required services exist)
 * services.validateRequired(ProfileManager.class, GearBuilder.class);
 *
 * // Retrieval (anywhere in code)
 * ProfileManager profiles = services.require(ProfileManager.class);
 * GearBuilder builder = services.require(GearBuilder.class);
 * }</pre>
 *
 * <p><b>Thread Safety:</b> This class is thread-safe and can be accessed concurrently.
 */
public class PluginServices {

  /** Thread-safe map of service instances */
  private final Map<Class<?>, Object> services = new ConcurrentHashMap<>();

  /** Thread-safe map of lazy service factories */
  private final Map<Class<?>, Supplier<?>> lazyFactories = new ConcurrentHashMap<>();

  /**
   * Registers a service instance for the given class type.
   *
   * <p>You can register the same instance under multiple types (e.g., concrete class and
   * interface):
   *
   * <pre>{@code
   * ProfileManager manager = new ProfileManager(...);
   * services.register(ProfileManager.class, manager);        // Concrete type
   * services.register(IProfileManager.class, manager);       // Interface
   * }</pre>
   *
   * @param <T> The service type
   * @param clazz The class representing the service type
   * @param instance The service instance to register
   * @return This services instance for method chaining
   * @throws IllegalArgumentException if clazz or instance is null
   * @throws IllegalStateException if a service of this type is already registered
   */
  public <T> PluginServices register(Class<T> clazz, T instance) {
    if (clazz == null) {
      throw new IllegalArgumentException("Service class cannot be null");
    }
    if (instance == null) {
      throw new IllegalArgumentException(
          "Service instance cannot be null for type: " + clazz.getSimpleName());
    }

    if (services.containsKey(clazz)) {
      throw new IllegalStateException(
          "Service already registered for type: "
              + clazz.getSimpleName()
              + ". Use registerOrReplace() if you want to override.");
    }

    services.put(clazz, instance);
    return this;
  }

  /**
   * Registers a lazy service factory for the given class type. The service will be created on first
   * access and cached for subsequent calls.
   *
   * <p>Useful for expensive-to-create services that may not be needed immediately.
   *
   * <p><b>Example:</b>
   *
   * <pre>{@code
   * services.registerLazy(ExpensiveService.class, () -> new ExpensiveService());
   * // Service not created yet
   *
   * ExpensiveService service = services.require(ExpensiveService.class);
   * // Service created now and cached
   * }</pre>
   *
   * @param <T> The service type
   * @param clazz The class representing the service type
   * @param factory A supplier that creates the service instance
   * @return This services instance for method chaining
   * @throws IllegalArgumentException if clazz or factory is null
   * @throws IllegalStateException if a service of this type is already registered
   */
  public <T> PluginServices registerLazy(Class<T> clazz, Supplier<T> factory) {
    if (clazz == null) {
      throw new IllegalArgumentException("Service class cannot be null");
    }
    if (factory == null) {
      throw new IllegalArgumentException(
          "Service factory cannot be null for type: " + clazz.getSimpleName());
    }

    if (services.containsKey(clazz) || lazyFactories.containsKey(clazz)) {
      throw new IllegalStateException(
          "Service already registered for type: " + clazz.getSimpleName());
    }

    lazyFactories.put(clazz, factory);
    return this;
  }

  /**
   * Registers a service instance for the given class type, replacing any existing registration. Use
   * this when you want to update a service after initial registration.
   *
   * @param <T> The service type
   * @param clazz The class representing the service type
   * @param instance The service instance to register
   * @return This services instance for method chaining
   * @throws IllegalArgumentException if clazz or instance is null
   */
  public <T> PluginServices registerOrReplace(Class<T> clazz, T instance) {
    if (clazz == null) {
      throw new IllegalArgumentException("Service class cannot be null");
    }
    if (instance == null) {
      throw new IllegalArgumentException(
          "Service instance cannot be null for type: " + clazz.getSimpleName());
    }

    services.put(clazz, instance);
    lazyFactories.remove(clazz); // Remove lazy factory if present
    return this;
  }

  /**
   * Retrieves a service instance for the given class type. If the service is lazy-registered, it
   * will be created on first access and cached.
   *
   * @param <T> The service type
   * @param clazz The class representing the service type
   * @return An Optional containing the service instance, or empty if not registered
   * @throws IllegalArgumentException if clazz is null
   */
  @SuppressWarnings("unchecked")
  public <T> Optional<T> get(Class<T> clazz) {
    if (clazz == null) {
      throw new IllegalArgumentException("Service class cannot be null");
    }

    // Check if already instantiated
    Object instance = services.get(clazz);
    if (instance != null) {
      return Optional.of((T) instance);
    }

    // Check if lazy factory exists
    Supplier<?> factory = lazyFactories.get(clazz);
    if (factory != null) {
      // Create and cache the instance
      T lazyInstance = (T) factory.get();
      services.put(clazz, lazyInstance);
      lazyFactories.remove(clazz);
      return Optional.of(lazyInstance);
    }

    return Optional.empty();
  }

  /**
   * Retrieves a service instance for the given class type, throwing an exception if not found. Use
   * this when you're certain the service should exist.
   *
   * <p>The exception message includes a list of all registered services to aid debugging.
   *
   * @param <T> The service type
   * @param clazz The class representing the service type
   * @return The service instance
   * @throws IllegalStateException if no service is registered for this type
   * @throws IllegalArgumentException if clazz is null
   */
  public <T> T require(Class<T> clazz) {
    return get(clazz)
        .orElseThrow(
            () -> {
              String registeredServices =
                  services.keySet().stream()
                      .map(Class::getSimpleName)
                      .collect(Collectors.joining(", "));

              String lazyServices =
                  lazyFactories.keySet().stream()
                      .map(c -> c.getSimpleName() + " (lazy)")
                      .collect(Collectors.joining(", "));

              String allServices = registeredServices;
              if (!lazyServices.isEmpty()) {
                allServices =
                    allServices.isEmpty() ? lazyServices : allServices + ", " + lazyServices;
              }

              return new IllegalStateException(
                  "No service registered for type: "
                      + clazz.getSimpleName()
                      + "\nRegistered services: "
                      + (allServices.isEmpty() ? "(none)" : allServices));
            });
  }

  /**
   * Validates that all required service types are registered. Throws an exception listing all
   * missing services if any are not found.
   *
   * <p>Call this during plugin initialization to fail-fast if required services are missing.
   *
   * <p><b>Example:</b>
   *
   * <pre>{@code
   * services.validateRequired(
   *     ProfileManager.class,
   *     GearBuilder.class,
   *     SkinRegistry.class
   * );
   * }</pre>
   *
   * @param requiredTypes The service types that must be registered
   * @throws IllegalStateException if any required services are missing
   */
  @SafeVarargs
  public final void validateRequired(Class<?>... requiredTypes) {
    Set<String> missing =
        Set.of(requiredTypes).stream()
            .filter(clazz -> !has(clazz))
            .map(Class::getSimpleName)
            .collect(Collectors.toSet());

    if (!missing.isEmpty()) {
      throw new IllegalStateException("Missing required services: " + String.join(", ", missing));
    }
  }

  /**
   * Checks if a service is registered for the given class type. Returns true for both eager and
   * lazy-registered services.
   *
   * @param clazz The class to check
   * @return true if a service is registered, false otherwise
   * @throws IllegalArgumentException if clazz is null
   */
  public boolean has(Class<?> clazz) {
    if (clazz == null) {
      throw new IllegalArgumentException("Service class cannot be null");
    }
    return services.containsKey(clazz) || lazyFactories.containsKey(clazz);
  }

  /**
   * Unregisters a service for the given class type. Removes both eager and lazy registrations.
   *
   * @param clazz The class representing the service type
   * @return true if a service was removed, false if none was registered
   * @throws IllegalArgumentException if clazz is null
   */
  public boolean unregister(Class<?> clazz) {
    if (clazz == null) {
      throw new IllegalArgumentException("Service class cannot be null");
    }
    boolean removed = services.remove(clazz) != null;
    removed |= lazyFactories.remove(clazz) != null;
    return removed;
  }

  /**
   * Returns the number of registered services (both eager and lazy).
   *
   * @return The service count
   */
  public int size() {
    return services.size() + lazyFactories.size();
  }

  /**
   * Returns the number of instantiated services (excludes uninitialized lazy services).
   *
   * @return The instantiated service count
   */
  public int instantiatedCount() {
    return services.size();
  }

  /**
   * Returns a set of all registered service types. Includes both instantiated and lazy-registered
   * services.
   *
   * @return Unmodifiable set of service types
   */
  public Set<Class<?>> getRegisteredTypes() {
    Set<Class<?>> allTypes = ConcurrentHashMap.newKeySet();
    allTypes.addAll(services.keySet());
    allTypes.addAll(lazyFactories.keySet());
    return Set.copyOf(allTypes);
  }

  /** Clears all registered services. Typically used during plugin shutdown. */
  public void clear() {
    services.clear();
    lazyFactories.clear();
  }

  // ========== CONVENIENCE METHODS ==========
  // Add shortcuts for frequently used services here to improve IDE autocomplete
  // and reduce verbosity. These are optional but recommended for commonly accessed services.

  public PlayerDataManager playerDataManager() {
    return require(PlayerDataManager.class);
  }

  public BundleManager bundleManager() {
    return require(BundleManager.class);
  }
}
