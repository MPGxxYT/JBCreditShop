package me.mortaldev.jbcreditshop.utils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.mortaldev.jbcreditshop.records.Pair;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.ChatColor;

/**
 * * Utility class for handling text formatting and manipulation using MiniMessage. Provides methods
 * for formatting strings, removing color and decoration tags, serializing and deserializing
 * components, and converting between MiniMessage components and legacy color code strings.
 *
 * <p>v2.0.3
 *
 * <p>Optimized version with pattern caching and enum lookups.
 */
public class TextUtil {

  // ========== CACHED PATTERNS FOR PERFORMANCE ==========
  private static final Pattern TAG_OR_TEXT_PATTERN = Pattern.compile("(<[^>]+>)|([^<]+)");
  private static final Pattern HEX_COLOR_PATTERN = Pattern.compile("[0-9a-fA-F]{6}");
  private static final Pattern HEX_TAG_PATTERN = Pattern.compile("<#.{6}>|&#.{6}");
  private static final Pattern NON_WORD_CHARS = Pattern.compile("\\W+");
  private static final Pattern MULTIPLE_RESET_PATTERN = Pattern.compile("(&r)+");

  // ========== CACHED INSTANCES ==========
  private static final GsonComponentSerializer GSON_SERIALIZER = GsonComponentSerializer.gson();
  private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
  private static final PlainTextComponentSerializer PLAIN_TEXT_SERIALIZER =
      PlainTextComponentSerializer.plainText();

  // ========== CONSTANTS ==========
  private static final String DEFAULT_COLOR_KEY = "f"; // Minecraft white
  private static final String DEFAULT_FILE_FORMAT = "formatted_string";

  /**
   * Translates a string using an alternate color code character into a string that uses the
   * internal {@link ChatColor#COLOR_CHAR} code.
   *
   * @param textToTranslate the string to translate.
   * @return the translated string.
   */
  public static String colorize(String textToTranslate) {
    return ChatColor.translateAlternateColorCodes('&', textToTranslate);
  }

  /**
   * Formats the given string by trimming, removing edge special characters, and replacing non-word
   * characters with underscores.
   *
   * @param string the input string to be formatted
   * @return the formatted string
   */
  public static String fileFormat(String string) {
    if (string == null || string.isEmpty()) {
      return DEFAULT_FILE_FORMAT;
    }

    // Trim and replace non-word characters with underscores
    String result = NON_WORD_CHARS.matcher(string.trim()).replaceAll("_");

    // Remove leading/trailing underscores
    result = result.replaceAll("^_+|_+$", "");

    return result.isEmpty() ? DEFAULT_FILE_FORMAT : result;
  }

  public static String removeDecorAndColor(String string) {
    return removeColors(removeDecoration(string));
  }

  /**
   * Removes decoration tags from the given string.
   *
   * @param string the string from which to remove decoration tags
   * @return the string with decoration tags removed
   */
  public static String removeDecoration(String string) {
    return removeKeys(string, Decorations.getKeys());
  }

  /**
   * Removes color tags from the given string.
   *
   * @param string the string from which to remove color tags
   * @return the string with color tags removed
   */
  public static String removeColors(String string) {
    String result = removeKeys(string, Colors.getKeys());
    return HEX_TAG_PATTERN.matcher(result).replaceAll("");
  }

  /**
   * Helper method to remove legacy format keys from a string.
   *
   * @param string the input string
   * @param keys the keys to remove
   * @return the string with keys removed
   */
  private static String removeKeys(String string, String[] keys) {
    StringBuilder editString = new StringBuilder(string);
    for (String key : keys) {
      int index;
      String legacyKey = "&" + key;
      while ((index = editString.indexOf(legacyKey)) != -1) {
        editString.delete(index, index + legacyKey.length());
      }
    }
    return editString.toString();
  }

  /**
   * Serializes a Component object to a JSON string using GsonComponentSerializer.
   *
   * @param component The Component object to serialize.
   * @return The serialized JSON representation of the Component object.
   */
  public static String serializeComponent(Component component) {
    return GSON_SERIALIZER.serialize(component);
  }

  public static String serializeComponent(String string) {
    return serializeComponent(format(string));
  }

  public static Component deserializeComponent(String string) {
    return GSON_SERIALIZER.deserialize(string);
  }

  public static String componentToString(Component component) {
    return PLAIN_TEXT_SERIALIZER.serialize(component);
  }

  public static String chatComponentToString(Component component) {
    return component instanceof TextComponent ? ((TextComponent) component).content() : "";
  }

  /**
   * Formats the given string using MiniMessage format tags and returns it as a Component object.
   * This method first converts legacy Minecraft formatting codes (e.g., &c, &#RRGGBB, &l) to
   * MiniMessage tags, then processes custom "##key:value" parameters for advanced features like
   * click/hover events.
   *
   * @param str the string to be formatted, potentially containing legacy codes and custom params.
   * @return the formatted string as a Component object.
   */
  public static Component format(String str) {
    if (str == null) {
      return Component.empty();
    }
    //    Main.debugLog("str: " + str);
    // 1. Process custom "##key:value" parameters and convert legacy codes selectively.
    //    asParam will now handle calling convertLegacyToMiniMessage internally.
    String finalMiniMessageString = asParam(str);

    //    Main.debugLog("final: " + finalMiniMessageString);
    // 2. Deserialize the fully formed MiniMessage string into a Component.
    return MINI_MESSAGE
        .deserialize(finalMiniMessageString)
        .decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE);
  }

  /**
   * Converts a string with legacy Minecraft formatting codes (& or §) into a MiniMessage string.
   * Uses optimized enum lookups instead of iteration.
   *
   * @param legacyText The string containing legacy formatting codes.
   * @return A string with MiniMessage tags.
   */
  private static String convertLegacyToMiniMessage(String legacyText) {
    if (legacyText == null || legacyText.isEmpty()) {
      return "";
    }

    StringBuilder out = new StringBuilder();
    String textToProcess = legacyText.replace('§', '&');

    String activeColorTag = null;
    EnumSet<Decorations> activeDecorations = EnumSet.noneOf(Decorations.class);

    for (int i = 0; i < textToProcess.length(); i++) {
      char currentChar = textToProcess.charAt(i);
      if (currentChar == '&') {
        if (i + 1 < textToProcess.length()) {
          char nextChar = textToProcess.charAt(i + 1);
          boolean consumed = false;

          // Try hex &#RRGGBB (8 chars: &#RRGGBB)
          if (nextChar == '#' && i + 7 < textToProcess.length()) {
            String hexCandidate = textToProcess.substring(i + 2, i + 8);
            if (HEX_COLOR_PATTERN.matcher(hexCandidate).matches()) {
              closeAllActiveStyles(out, activeColorTag, activeDecorations);
              activeColorTag = "<#" + hexCandidate + ">";
              out.append(activeColorTag);
              i += 7; // Consumed &#RRGGBB
              consumed = true;
            }
          }

          if (!consumed) {
            Colors matchedColor = Colors.fromKey(nextChar);

            if (matchedColor != null) {
              closeAllActiveStyles(out, activeColorTag, activeDecorations);
              activeColorTag = "<" + matchedColor.getValue() + ">";
              out.append(activeColorTag);
              i += 1;
              consumed = true;
            } else {
              Decorations matchedDecoration = Decorations.fromKey(nextChar);

              if (matchedDecoration != null) {
                if (matchedDecoration == Decorations.RESET) {
                  closeAllActiveStyles(out, activeColorTag, activeDecorations);
                  activeColorTag = null;
                } else {
                  activeDecorations.add(matchedDecoration); // EnumSet handles duplicates
                  out.append('<').append(matchedDecoration.getValue()).append('>');
                }
                i += 1;
                consumed = true;
              }
            }
          }

          if (!consumed) {
            out.append('&');
          }
        } else {
          out.append('&');
        }
      } else {
        out.append(currentChar);
      }
    }

    closeAllActiveStyles(out, activeColorTag, activeDecorations);

    return out.toString();
  }

  /**
   * Closes all active styles (decorations and colors) in the output.
   *
   * @param out the output StringBuilder
   * @param activeColorTag the currently active color tag
   * @param activeDecorations the currently active decorations
   */
  private static void closeAllActiveStyles(
      StringBuilder out, String activeColorTag, EnumSet<Decorations> activeDecorations) {
    // Close decorations in reverse order (LIFO)
    List<Decorations> decorationList = new ArrayList<>(activeDecorations);
    for (int i = decorationList.size() - 1; i >= 0; i--) {
      out.append("</").append(decorationList.get(i).getValue()).append('>');
    }
    activeDecorations.clear();

    if (activeColorTag != null) {
      if (activeColorTag.startsWith("<#")) {
        out.append("</#").append(activeColorTag.substring(2));
      } else {
        out.append("</").append(activeColorTag.substring(1));
      }
    }
  }

  /**
   * * Converts a MiniMessage Component back into a legacy color code string (e.g., "&aHello"). This
   * attempts to preserve colors and basic decorations but may not perfectly replicate complex
   * MiniMessage features like gradients, transitions, or nested hover/click events in a simple
   * legacy string.
   *
   * <p>It processes the MiniMessage string representation of the component, tracking the current
   * color and decoration states based on the tags encountered. When text is found, it compares the
   * current state to the last emitted legacy codes and appends the necessary legacy codes (&, §) to
   * transition to the current state before appending the text.
   *
   * <p>Note: This is a best-effort conversion. MiniMessage is more expressive than legacy codes.
   *
   * @param component The MiniMessage Component to deformat.
   * @return A string with legacy color and decoration codes.
   */
  public static String deformat(Component component) {
    String miniMessageStr = MINI_MESSAGE.serialize(component);

    StringBuilder legacyResult = new StringBuilder();

    // Style state trackers for the *last emitted* legacy codes
    String lastEmittedLegacyColorKey = DEFAULT_COLOR_KEY;
    EnumSet<Decorations> lastEmittedDecorations = EnumSet.noneOf(Decorations.class);

    // Style stack for MiniMessage processing
    Deque<String> colorTagStack = new ArrayDeque<>();
    colorTagStack.push("<white>"); // Base default color

    // Current decoration states based on MiniMessage tags encountered
    Map<Decorations, Boolean> currentDecorationStates = new EnumMap<>(Decorations.class);
    for (Decorations d : Decorations.values()) {
      currentDecorationStates.put(d, false);
    }

    // Use cached pattern
    Matcher matcher = TAG_OR_TEXT_PATTERN.matcher(miniMessageStr);

    while (matcher.find()) {
      String tag = matcher.group(1);
      String text = matcher.group(2);

      if (tag != null) {
        // --- Process MiniMessage Tag and Update Style Stack/State ---

        // 1. Handle Color Tags (and stack)
        if (tag.matches("<#[0-9a-fA-F]{6}>")) { // Hex color open: <#RRGGBB>
          colorTagStack.push(tag);
        } else if (tag.matches("</#[0-9a-fA-F]{6}>")) { // Hex color close: </#RRGGBB>
          if (colorTagStack.size() > 1) colorTagStack.pop(); // Don't pop the base default
        } else {
          for (Colors c : Colors.values()) {
            if (tag.equals("<" + c.getValue() + ">")) { // Named color open: <gray>
              colorTagStack.push(tag);
              break;
            } else if (tag.equals("</" + c.getValue() + ">")) { // Named color close: </gray>
              if (colorTagStack.size() > 1) colorTagStack.pop();
              break;
            }
          }
        }
        if (tag.equals("</color>")) { // Generic color closing tag
          if (colorTagStack.size() > 1) colorTagStack.pop();
        }

        // 2. Handle Decoration Tags
        for (Decorations d : Decorations.values()) {
          if (tag.equals("<" + d.getValue() + ">")) { // <bold>, <italic>, <reset>
            currentDecorationStates.put(d, true);
            if (d == Decorations.RESET) {
              // Reset all other decorations and color stack
              for (Decorations dReset : Decorations.values()) {
                currentDecorationStates.put(dReset, false); // Clear all
              }
              currentDecorationStates.put(Decorations.RESET, true); // Mark reset as active
              colorTagStack.clear();
              colorTagStack.push("<white>"); // Reset color to white
            }
            break;
          } else if (tag.equals("</" + d.getValue() + ">")) { // </bold>, </italic>
            // This implies the decoration should revert to its state before the opening tag.
            // For simplicity with legacy, we often just turn it off if not explicitly re-enabled.
            // MiniMessage usually makes the new state explicit with <!false_tag> or a new
            // <true_tag>.
            currentDecorationStates.put(d, false);
            break;
          } else if (tag.equals("<!" + d.getValue() + ">")) { // <!bold>, <!italic> (set to false)
            currentDecorationStates.put(d, false);
            // If this was <!reset>, it means "reset is false", which is the normal state.
            if (d == Decorations.RESET) currentDecorationStates.put(d, false);
            break;
          }
        }
        // Note: Tags like </!obfuscated> are non-standard MiniMessage and would need special
        // handling
        // if they appear. They are ignored here.

      } else if (text != null && !text.isEmpty()) {
        // --- This is a Text Segment: Generate Legacy Codes ---
        String currentMiniMessageColorTag = colorTagStack.peek(); // Should not be empty due to base
        String targetLegacyColorKey =
            convertMiniMessageColorToLegacyKey(currentMiniMessageColorTag);

        EnumSet<Decorations> targetDecorations = EnumSet.noneOf(Decorations.class);
        boolean resetApplied = false;
        if (currentDecorationStates.getOrDefault(Decorations.RESET, false)) {
          targetDecorations.add(Decorations.RESET);
          resetApplied = true;
        } else {
          for (Map.Entry<Decorations, Boolean> entry : currentDecorationStates.entrySet()) {
            if (entry.getValue() && entry.getKey() != Decorations.RESET) {
              targetDecorations.add(entry.getKey());
            }
          }
        }

        // Compare target style with lastEmittedStyle and append changes
        if (resetApplied) {
          // If reset was just activated
          if (!lastEmittedDecorations.contains(Decorations.RESET)) {
            legacyResult.append("&r");
          }
          lastEmittedLegacyColorKey =
              getDefaultLegacyColorKey(); // &r resets to default (e.g., white)
          lastEmittedDecorations.clear();
          lastEmittedDecorations.add(Decorations.RESET);

          // If the target color after reset is not the default, apply it
          if (!targetLegacyColorKey.equals(lastEmittedLegacyColorKey)) {
            legacyResult.append(convertLegacyColorKeyToCode(targetLegacyColorKey));
            lastEmittedLegacyColorKey = targetLegacyColorKey;
          }
          // Apply any decorations that are true *after* reset (should be none if only <reset> was
          // seen)
          // This part handles if <reset><bold>TEXT which becomes &r&lTEXT
          for (Decorations deco : targetDecorations) {
            if (deco != Decorations.RESET) { // RESET is already handled by &r
              legacyResult.append("&").append(deco.getKey());
              lastEmittedDecorations.add(deco); // Track it
            }
          }

        } else { // Not a reset context
          boolean colorActuallyChanged = !targetLegacyColorKey.equals(lastEmittedLegacyColorKey);

          if (colorActuallyChanged) {
            legacyResult.append(convertLegacyColorKeyToCode(targetLegacyColorKey));
            lastEmittedLegacyColorKey = targetLegacyColorKey;
            lastEmittedDecorations.clear(); // Color change resets decorations

            // Apply all target decorations
            for (Decorations deco : targetDecorations) {
              legacyResult.append("&").append(deco.getKey());
              lastEmittedDecorations.add(deco);
            }
          } else {
            // Color is the same as last emitted. Check for decoration changes.
            EnumSet<Decorations> decosToTurnOn = EnumSet.copyOf(targetDecorations);
            decosToTurnOn.removeAll(
                lastEmittedDecorations); // Decorations that are now true but were false

            EnumSet<Decorations> decosToTurnOff = EnumSet.copyOf(lastEmittedDecorations);
            decosToTurnOff.removeAll(targetDecorations);
            decosToTurnOff.remove(Decorations.RESET); // RESET is handled by its own block

            if (!decosToTurnOff.isEmpty()) {
              // **CRUCIAL CHANGE HERE:** A decoration needs to be turned OFF, and color is the
              // same.
              // To reliably turn off decorations (especially if re-applying same color doesn't
              // work),
              // use &r, then re-apply the target color and all target decorations.
              legacyResult.append("&r");
              lastEmittedLegacyColorKey =
                  getDefaultLegacyColorKey(); // Color is now default (e.g., "f")
              lastEmittedDecorations.clear();
              lastEmittedDecorations.add(Decorations.RESET); // Mark that &r was applied

              // Re-apply the target color if it's not the default color that &r already set
              if (!targetLegacyColorKey.equals(lastEmittedLegacyColorKey)) {
                legacyResult.append(convertLegacyColorKeyToCode(targetLegacyColorKey));
                lastEmittedLegacyColorKey = targetLegacyColorKey;
                // After a color code, legacy decorations are reset, so clear from our tracking
                // (except RESET itself which is now 'off' effectively by the color)
                lastEmittedDecorations.remove(Decorations.RESET);
              }

              // Apply ALL decorations that should be active for this segment
              for (Decorations deco :
                  targetDecorations) { // targetDecorations contains only what should be TRUE
                if (deco != Decorations.RESET) { // RESET state is already handled
                  legacyResult.append("&").append(deco.getKey());
                  lastEmittedDecorations.add(deco);
                }
              }
            } else if (!decosToTurnOn.isEmpty()) {
              // No decorations to turn OFF, only new ones to turn ON. Color is the same.
              // This case is simpler: just append the new decoration codes.
              for (Decorations deco : decosToTurnOn) {
                if (deco != Decorations.RESET) { // Should not encounter RESET here normally
                  legacyResult.append("&").append(deco.getKey());
                  lastEmittedDecorations.add(deco);
                }
              }
            }
          }
        }
        legacyResult.append(text);
      }
    }

    // Final cleanup for multiple &r using cached pattern
    String finalStr = legacyResult.toString();
    return MULTIPLE_RESET_PATTERN.matcher(finalStr).replaceAll("&r");
  }

  // Helper to get legacy color code string (e.g., "&f", "&#RRGGBB")
  private static String convertLegacyColorKeyToCode(String legacyColorKey) {
    if (legacyColorKey.length() == 6) { // Hex RRGGBB
      return "&#" + legacyColorKey;
    } else { // Single char key like "f"
      return "&" + legacyColorKey;
    }
  }

  // Helper to get just the key part ("f" or "RRGGBB")
  private static String convertMiniMessageColorToLegacyKey(String miniMessageColorTag) {
    if (miniMessageColorTag.startsWith("<#")
        && miniMessageColorTag.length() == 9
        && miniMessageColorTag.endsWith(">")) {
      return miniMessageColorTag.substring(2, 8); // RRGGBB
    }
    for (Colors c : Colors.values()) {
      if (miniMessageColorTag.equals("<" + c.getValue() + ">")) {
        return c.getKey(); // "f", "7", etc.
      }
    }
    return getDefaultLegacyColorKey(); // Default if unknown
  }

  private static String getDefaultLegacyColorKey() {
    return DEFAULT_COLOR_KEY;
  }

  private static String asParam(String rawLegacyString) {
    if (rawLegacyString == null || rawLegacyString.isEmpty()) {
      return "";
    }

    String[] rawSegments = rawLegacyString.split("##", -1);

    if (rawSegments.length == 0) return "";

    // Build the final result piece by piece
    StringBuilder resultBuilder = new StringBuilder(convertLegacyToMiniMessage(rawSegments[0]));

    // Track the current text segment that actions should be applied to
    StringBuilder currentSegmentBuilder = new StringBuilder();
    List<Pair<Types, String>> activeNestableActions = new ArrayList<>();

    for (int i = 1; i < rawSegments.length; i++) {
      String currentRawSegment = rawSegments[i];

      if (currentRawSegment.isEmpty()
          && i < rawSegments.length - 1
          && rawSegments[i + 1].isEmpty()) {
        continue;
      }

      String tagKey = (currentRawSegment.length() >= 4) ? currentRawSegment.substring(0, 4) : "";
      Types type = Types.getTypeFromKey(tagKey);

      if (type != null) { // It's a ##tag:value## segment
        String legacyTagValue = currentRawSegment.substring(4);

        if (isNestableActionType(type)) {
          // Accumulate nestable actions to apply to the current segment
          activeNestableActions.add(new Pair<>(type, legacyTagValue));
        } else {
          // Non-nestable tag: finalize current segment first, then apply this tag to everything
          if (!activeNestableActions.isEmpty()) {
            String processedSegment =
                applyNestedActionTags(currentSegmentBuilder.toString(), activeNestableActions);
            resultBuilder.append(processedSegment);
            currentSegmentBuilder.setLength(0);
            activeNestableActions.clear();
          } else if (!currentSegmentBuilder.isEmpty()) {
            resultBuilder.append(currentSegmentBuilder);
            currentSegmentBuilder.setLength(0);
          }

          // Apply the current non-nestable tag to all accumulated result
          String processedNonNestable =
              performTypeValueReplacement(type.getKey(), legacyTagValue, resultBuilder.toString());
          resultBuilder.setLength(0);
          resultBuilder.append(processedNonNestable);
        }
      } else { // Segment is not a recognized tag, so it's plain text.
        // Process any pending nestable actions on the previous segment first
        if (!activeNestableActions.isEmpty()) {
          String processedSegment =
              applyNestedActionTags(currentSegmentBuilder.toString(), activeNestableActions);
          resultBuilder.append(processedSegment);
          currentSegmentBuilder.setLength(0);
          activeNestableActions.clear();
        } else if (!currentSegmentBuilder.isEmpty()) {
          // Flush current segment to result
          resultBuilder.append(currentSegmentBuilder);
          currentSegmentBuilder.setLength(0);
        }

        // Start a new segment with this text
        if (!currentRawSegment.isEmpty()
            || (i == rawSegments.length - 1 && rawLegacyString.endsWith("##"))) {
          currentSegmentBuilder.append(convertLegacyToMiniMessage(currentRawSegment));
        }
      }
    }

    // Process any remaining nestable actions at the end
    if (!activeNestableActions.isEmpty()) {
      String processedSegment =
          applyNestedActionTags(currentSegmentBuilder.toString(), activeNestableActions);
      resultBuilder.append(processedSegment);
    } else if (!currentSegmentBuilder.isEmpty()) {
      resultBuilder.append(currentSegmentBuilder);
    }

    return resultBuilder.toString();
  }

  private static boolean isNestableActionType(Types type) {
    if (type == null) return false;
    // Define which types are considered for special nesting (e.g., click/hover)
    return switch (type) {
      case CHANGE_PAGE,
          COPY_TO_CLIPBOARD,
          OPEN_FILE,
          OPEN_PAGE,
          RUN_COMMAND,
          SUGGEST_COMMAND,
          SHOW_ENTITY,
          SHOW_ITEM,
          SHOW_TEXT,
          INSERT -> // Insert can also be part of nesting
          true;
      default -> false;
    };
  }

  private static String applyNestedActionTags(String inputText, List<Pair<Types, String>> actions) {
    String currentText = inputText;

    // Define a hierarchy for nesting. Example: Click > Hover > Insert
    // This is a simplified example. More complex logic might be needed for many action types.
    Pair<Types, String> clickAction = null;
    Pair<Types, String> hoverAction = null;
    Pair<Types, String> insertAction = null;

    for (Pair<Types, String> action : actions) {
      switch (action.first()) {
        // Click-like actions
        case OPEN_PAGE:
        case CHANGE_PAGE:
        case COPY_TO_CLIPBOARD:
        case OPEN_FILE:
        case RUN_COMMAND:
        case SUGGEST_COMMAND:
          clickAction = action; // Last one wins if multiple specified
          break;
        // Hover-like actions
        case SHOW_ENTITY:
        case SHOW_ITEM:
        case SHOW_TEXT:
          hoverAction = action; // Last one wins
          break;
        // Insert action
        case INSERT:
          insertAction = action; // Last one wins
          break;
        default:
          // Should not happen if isNestableActionType is aligned
          break;
      }
    }

    // Apply in reverse order of desired nesting (innermost first)
    if (insertAction != null) {
      currentText =
          performTypeValueReplacement(
              insertAction.first().getKey(), insertAction.second(), currentText);
    }
    if (hoverAction != null) {
      currentText =
          performTypeValueReplacement(
              hoverAction.first().getKey(), hoverAction.second(), currentText);
    }
    if (clickAction != null) {
      currentText =
          performTypeValueReplacement(
              clickAction.first().getKey(), clickAction.second(), currentText);
    }
    return currentText;
  }

  private static String escapeMiniMessageArgument(String arg) {
    if (arg == null) return "";
    return arg.replace("'", "\\'");
  }

  private static boolean typeTakesMiniMessageArgument(Types type) {
    if (type == null) return false;
    // Only types where the #arg# itself is displayed as text and can have formatting
    return type == Types.SHOW_TEXT;
  }

  private static String performTypeValueReplacement(
      String tagKey, String legacyArgValue, String miniMessageInputText) {
    Types type = Types.getTypeFromKey(tagKey);
    if (type != null) {
      String finalArgForMiniMessageTag;
      if (typeTakesMiniMessageArgument(type)) {
        finalArgForMiniMessageTag = convertLegacyToMiniMessage(legacyArgValue);
      } else {
        // For args like URLs, command strings, page names, font names, etc.,
        // which are not themselves MiniMessage.
        finalArgForMiniMessageTag = legacyArgValue;
      }
      String escapedFinalArg = escapeMiniMessageArgument(finalArgForMiniMessageTag);
      return type.getValue()
          .replace("#arg#", escapedFinalArg)
          .replace("#input#", miniMessageInputText);
    }
    return miniMessageInputText; // Should not happen if type was resolved
  }

  private enum Types {
    // CLICK ACTIONS
    CHANGE_PAGE("pge:", "<click:change_page:'#arg#'>#input#</click>"),
    COPY_TO_CLIPBOARD("cpy:", "<click:copy_to_clipboard:'#arg#'>#input#</click>"),
    OPEN_FILE("fle:", "<click:open_file:'#arg#'>#input#</click>"),
    OPEN_PAGE("url:", "<click:open_url:'#arg#'>#input#</click>"),
    RUN_COMMAND("cmd:", "<click:run_command:'#arg#'>#input#</click>"),
    SUGGEST_COMMAND("sgt:", "<click:suggest_command:'#arg#'>#input#</click>"),

    // HOVER
    SHOW_ENTITY("ent:", "<hover:show_entity:'#arg#'>#input#</hover>"),
    SHOW_ITEM("itm:", "<hover:show_item:#arg#>#input#</hover>"),
    SHOW_TEXT("ttp:", "<hover:show_text:'#arg#'>#input#</hover>"),

    // KEYBIND
    KEY("key:", "#input#<key:#arg#>"),

    // TRANSLATE
    // ex. ##lng:block.minecraft.diamond_block
    // ex. ##lng:commands.drop.success.single:'<red>1':'<blue>Stone'
    LANG("lng:", "#input#<lang:#arg#>"),

    // INSERT
    INSERT("ins:", "<insert:'#arg#'>#input#</insert>"),

    // RAINBOW
    // COLORS##rnb:##no colors
    RAINBOW("rnb:", "<rainbow>#input#</rainbow>"),

    // GRADIENT
    // colored##grd:#5e4fa2:#f79459##not colored
    // colored##grd:#5e4fa2:#f79459:red##not colored
    // colored##grd:green:blue##not colored
    GRADIENT("grd:", "<gradient:#arg#>#input#</gradient>"),

    // TRANSITION
    // colored##trn:[color1]:[color...]:[phase]##not colored
    // colored##trn:#00ff00:#ff0000:0##not colored
    TRANSITION("trn:", "<transition:#arg#>#input#</transition>"),

    // FONT
    FONT("fnt:", "<font:#arg#>#input#</font>"),

    // SELECTOR
    // Hello ##slt:@e[limit=5]##, I'm ##slt:@s##!
    SELECTOR("slt:", "#input#<selector:#arg#>"),

    // SCORE
    // ##score:_name_:_objective_##
    // You have won ##scr:rymiel:gamesWon/## games!
    SCORE("scr:", "#input#<score:#arg#>"),

    // NBT
    // ##nbt:block|armorStand|storage:id:path[:_separator_][:interpret]##
    // Your health is ##nbt:armorStand:'@s':Health/##
    NBT("nbt:", "#input#<nbt:#arg#>");

    private final String key;
    private final String value;

    private static final String[] KEYS_CACHE;
    private static final Map<String, Types> KEY_TO_TYPE_MAP;

    static {
      KEYS_CACHE = Arrays.stream(values()).map(Types::getKey).toArray(String[]::new);

      // Use HashMap for String keys
      Map<String, Types> map = new HashMap<>();
      for (Types type : values()) {
        map.put(type.getKey(), type);
      }
      KEY_TO_TYPE_MAP = Collections.unmodifiableMap(map);
    }

    Types(String key, String value) {
      this.key = key;
      this.value = value;
    }

    static String[] getKeys() {
      return KEYS_CACHE;
    }

    static Types getTypeFromKey(String string) {
      return KEY_TO_TYPE_MAP.get(string);
    }

    String getKey() {
      return key;
    }

    String getValue() {
      return value;
    }
  }

  private enum Decorations {
    BOLD("l", "bold"),
    ITALIC("o", "italic"),
    UNDERLINE("n", "underlined"),
    STRIKETHROUGH("m", "strikethrough"),
    OBFUSCATED("k", "obfuscated"),
    RESET("r", "reset");

    private final String key;
    private final String value;

    // Cache for getKeys()
    private static final String[] KEYS_CACHE;
    // O(1) lookup map for fromKey
    private static final Map<Character, Decorations> KEY_LOOKUP = new HashMap<>();

    static {
      KEYS_CACHE = Arrays.stream(values()).map(Decorations::getKey).toArray(String[]::new);
      for (Decorations decoration : values()) {
        KEY_LOOKUP.put(decoration.getKey().charAt(0), decoration);
      }
    }

    Decorations(String key, String value) {
      this.key = key;
      this.value = value;
    }

    static String[] getKeys() {
      return KEYS_CACHE;
    }

    /**
     * Fast O(1) lookup of decoration by character key.
     *
     * @param keyChar the character key
     * @return the Decorations enum, or null if not found
     */
    static Decorations fromKey(char keyChar) {
      return KEY_LOOKUP.get(keyChar);
    }

    String getKey() {
      return key;
    }

    String getValue() {
      return value;
    }
  }

  private enum Colors {
    BLACK("0", "black"),
    DARK_BLUE("1", "dark_blue"),
    DARK_GREEN("2", "dark_green"),
    DARK_AQUA("3", "dark_aqua"),
    DARK_RED("4", "dark_red"),
    DARK_PURPLE("5", "dark_purple"),
    GOLD("6", "gold"),
    GREY("7", "gray"),
    DARK_GREY("8", "dark_gray"),
    BLUE("9", "blue"),
    GREEN("a", "green"),
    AQUA("b", "aqua"),
    RED("c", "red"),
    LIGHT_PURPLE("d", "light_purple"),
    YELLOW("e", "yellow"),
    WHITE("f", "white");

    private final String key;
    private final String value;

    // Cache for getKeys()
    private static final String[] KEYS_CACHE;
    // O(1) lookup map for fromKey
    private static final Map<Character, Colors> KEY_LOOKUP = new HashMap<>();

    static {
      KEYS_CACHE = Arrays.stream(values()).map(Colors::getKey).toArray(String[]::new);
      for (Colors color : values()) {
        KEY_LOOKUP.put(color.getKey().charAt(0), color);
      }
    }

    Colors(String key, String value) {
      this.key = key;
      this.value = value;
    }

    static String[] getKeys() {
      return KEYS_CACHE;
    }

    /**
     * Fast O(1) lookup of color by character key.
     *
     * @param keyChar the character key
     * @return the Colors enum, or null if not found
     */
    static Colors fromKey(char keyChar) {
      return KEY_LOOKUP.get(keyChar);
    }

    String getKey() {
      return key;
    }

    String getValue() {
      return value;
    }
  }
}
