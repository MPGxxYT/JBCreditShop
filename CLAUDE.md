# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

```bash
mvn clean package          # Build the plugin JAR (default goal)
mvn test                   # Run JUnit tests
mvn test -Dtest=TestClass  # Run a single test class
```

The build uses Maven Shade plugin to relocate AnvilGUI and ACF dependencies into the plugin's namespace. Output JAR is in `target/`.

## Project Overview

JBCreditShop is a Paper/Bukkit Minecraft plugin (Java 17) for managing credit-based shops using EcoBits currency. Players can purchase items, commands, and permissions.

**Required Server Dependencies:** EcoBits, LuckPerms

## Architecture

### Service Registry Pattern
`PluginServices` provides thread-safe dependency injection. Register services with `register(key, supplier)` and retrieve with `get(key)` or convenience methods like `playerDataManager()`.

### Dual Persistence Strategy
- **YAML**: Shops and ShopItems (human-editable config in `shops/` and `shopitems/`)
- **JSON via AbstractCRUD**: PlayerData, Bundles, Transactions, ShopStats (data records)

### Manager Singletons
Each data domain has a Manager class (thread-safe singleton via static inner holder):
- `ShopManager` / `ShopItemsManager` - YAML-backed with in-memory cache
- `PlayerDataManager`, `BundleManager`, `TransactionLogManager` - CRUD-backed

### Menu System
- `BasicMenu<T>` - Generic paginated inventory template (45 items/page) with search/sort
- Two shop styles: `AUTO` (paginated) and `CUSTOM` (manual slot placement with configurable rows)
- AnvilGUI integration for text input dialogs

### Builder Pattern
`Shop.Builder`, `ShopItem.Builder`, and `ItemStackHelper.Builder` provide fluent APIs for object construction.

### ItemStack Serialization
Custom Jackson serializers in `serializers/` handle ItemStack via Bukkit's `serializeAsBytes()` to Base64.

## Key Entry Points

- `Main.java` - Plugin lifecycle, dependency validation, initialization
- `CreditShopCommand.java` - Main command `/creditshop` (alias `/crshop`)
- `BundleCommand.java` - Bundle management subcommands

## Data Flow

1. YAML configs loaded on startup → in-memory caches
2. Menu system reads from managers
3. Purchases: EcoBits deduction → reward delivery (item/command/permission via LuckPerms)
4. Player history persisted via CRUD to JSON

## Discount System

Shop-level discount applies to all items; item-level discount can override. Optional discount groups (e.g., "Easter") for targeted promotions. Items control `allow_discount_stacking`.
