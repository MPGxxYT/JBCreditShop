package me.mortaldev.jbcreditshop.utils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.mortaldev.jbcreditshop.Main;
import me.mortaldev.jbcreditshop.records.Pair;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

/**
 * * Utility class for handling text formatting and manipulation using MiniMessage. Provides methods
 * for formatting strings, removing color and decoration tags, serializing and deserializing
 * components, and converting between MiniMessage components and legacy color code strings.
 *
 * <p>v2.0.0
 *
 * <p>Remade formatting system.
 */
public class TextUtil {

  /**
   * Formats the given string by trimming, removing edge special characters, and replacing non-word
   * characters with underscores.
   *
   * @param string the input string to be formatted
   * @return the formatted string
   */
  public static String fileFormat(String string) {
    // Trim leading and trailing whitespace
    string = string.trim();

    // Remove leading and trailing special characters (like ., -, _, etc.)
    string = string.replaceAll("^[^a-zA-Z0-9]+|[^a-zA-Z0-9]+$", "");

    // Replace any sequence of non-word characters with a single underscore
    string = string.replaceAll("\\W+", "_");

    // Handle potential empty string after formatting
    if (string.isEmpty()) {
      // Return a default or throw an exception, depending on desired behavior
      // For now, returning a simple default like "formatted_string"
      return "formatted_string";
    }

    return string;
  }

  /**
   * Removes decoration tags from the given string.
   *
   * @param string the string from which to remove decoration tags
   * @return the string with decoration tags removed
   */
  public static String removeDecoration(String string) {
    StringBuilder editString = new StringBuilder(string);
    for (String key : Decorations.getKeys()) {
      // Avoid creating new strings in loop for replacement if possible
      // String.replace creates a new string. For StringBuilder, use its replace.
      int index;
      String legacyKey = "&" + key;
      while ((index = editString.indexOf(legacyKey)) != -1) {
        editString.delete(index, index + legacyKey.length());
      }
    }
    return editString.toString();
  }

  /**
   * Removes color tags from the given string.
   *
   * @param string the string from which to remove color tags
   * @return the string with color tags removed
   */
  public static String removeColors(String string) {
    StringBuilder editString = new StringBuilder(string);
    for (String key : Colors.getKeys()) {
      int index;
      String legacyKey = "&" + key;
      while ((index = editString.indexOf(legacyKey)) != -1) {
        editString.delete(index, index + legacyKey.length());
      }
    }
    // For hex, regex is fine on the final string
    return editString.toString().replaceAll("<#.{6}>", "").replaceAll("&#.{6}", "");
  }

  /**
   * Serializes a Component object to a JSON string using GsonComponentSerializer.
   *
   * @param component The Component object to serialize.
   * @return The serialized JSON representation of the Component object.
   */
  public static String serializeComponent(Component component) {
    return GsonComponentSerializer.gson().serialize(component);
  }

  public static String serializeComponent(String string) {
    return serializeComponent(format(string));
  }

  public static Component deserializeComponent(String string) {
    return GsonComponentSerializer.gson().deserialize(string);
  }

  public static String componentToString(Component component) {
    return PlainTextComponentSerializer.plainText().serialize(component);
  }

  public static String chatComponentToString(Component component) {
    return component instanceof TextComponent ? ((TextComponent) component).content() : "";
  }

  /**
   * @deprecated This method is redundant and will be removed. Use {@link #format(String)} instead.
   */
  @Deprecated
  public static Component format(String str, boolean deprecated) {
    return format(str);
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
      return Component
          .empty(); // Or throw new IllegalArgumentException("Input string cannot be null.");
    }
    Main.debugLog("str: " + str);
    // 1. Process custom "##key:value" parameters and convert legacy codes selectively.
    //    asParam will now handle calling convertLegacyToMiniMessage internally.
    String finalMiniMessageString = asParam(str);

    Main.debugLog("final: " + finalMiniMessageString);
    // 2. Deserialize the fully formed MiniMessage string into a Component.
    return MiniMessage.miniMessage()
        .deserialize(finalMiniMessageString)
        .decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE);
  }

  /**
   * Converts a string with legacy Minecraft formatting codes (& or §) into a MiniMessage string. -
   * Color codes (e.g., &c) are converted to <reset><color_name> (e.g., <reset><red>). - Hex color
   * codes (e.g., &#RRGGBB) are converted to <#RRGGBB>. - Decoration codes (e.g., &l) are converted
   * to <decoration_name> (e.g., <bold>). - &r is converted to <reset>. - &nl is converted to
   * <newline>. - Unrecognized & codes are passed through literally.
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
    List<Decorations> activeDecorations = new ArrayList<>();

    for (int i = 0; i < textToProcess.length(); i++) {
      char currentChar = textToProcess.charAt(i);
      if (currentChar == '&') {
        if (i + 1 < textToProcess.length()) {
          char nextChar = textToProcess.charAt(i + 1);
          boolean consumed = false;

          // REMOVED &nl special handling block
          // if (nextChar == 'n' /* ... &nl specific checks ... */ ) { /* ... */ }

          // Try hex &#RRGGBB (8 chars: &#RRGGBB)
          if (nextChar == '#'
              && i + 7 < textToProcess.length()) { // Check this before single char codes
            String hexCandidate = textToProcess.substring(i + 2, i + 8);
            if (hexCandidate.matches("[0-9a-fA-F]{6}")) {
              closeAllActiveStyles(out, activeColorTag, activeDecorations);
              activeColorTag = "<#" + hexCandidate + ">";
              out.append(activeColorTag);
              i += 7; // Consumed &#RRGGBB
              consumed = true;
            }
          }

          if (!consumed) {
            Colors matchedColor = null;
            for (Colors color : Colors.values()) {
              if (color.getKey().charAt(0) == nextChar) {
                matchedColor = color;
                break;
              }
            }

            if (matchedColor != null) {
              closeAllActiveStyles(out, activeColorTag, activeDecorations);
              activeColorTag = "<" + matchedColor.getValue() + ">";
              out.append(activeColorTag);
              i += 1;
              consumed = true;
            } else {
              Decorations matchedDecoration = null;
              for (Decorations decoration : Decorations.values()) {
                if (decoration.getKey().charAt(0) == nextChar) {
                  matchedDecoration = decoration;
                  break;
                }
              }
              if (matchedDecoration != null) {
                if (matchedDecoration == Decorations.RESET) {
                  closeAllActiveStyles(out, activeColorTag, activeDecorations);
                  activeColorTag = null;
                } else {
                  if (!activeDecorations.contains(matchedDecoration)) {
                    activeDecorations.add(matchedDecoration);
                  }
                  out.append("<").append(matchedDecoration.getValue()).append(">");
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

  private static void closeAllActiveStyles(
      StringBuilder out, String activeColorTag, List<Decorations> activeDecorations) {
    for (int i = activeDecorations.size() - 1; i >= 0; i--) {
      out.append("</").append(activeDecorations.get(i).getValue()).append(">");
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
    String miniMessageStr = MiniMessage.miniMessage().serialize(component);
    //    System.out.println("miniMessageStr = " + miniMessageStr);

    StringBuilder legacyResult = new StringBuilder();

    // Style state trackers for the *last emitted* legacy codes
    String lastEmittedLegacyColorKey = "f"; // Default Minecraft white's key
    EnumSet<Decorations> lastEmittedDecorations = EnumSet.noneOf(Decorations.class);

    // Style stack for MiniMessage processing
    Deque<String> colorTagStack =
        new ArrayDeque<>(); // Stores MiniMessage color tags like "<gray>", "<#RRGGBB>"
    colorTagStack.push("<white>"); // Base default color

    // Current decoration states based on MiniMessage tags encountered
    Map<Decorations, Boolean> currentDecorationStates = new EnumMap<>(Decorations.class);
    for (Decorations d : Decorations.values()) {
      // Set initial decoration states (e.g., italic is often false by default in Components)
      currentDecorationStates.put(d, false);
    }

    // Regex to find tags or text: group 1 is tag, group 2 is text
    Pattern pattern = Pattern.compile("(<[^>]+>)|([^<]+)");
    Matcher matcher = pattern.matcher(miniMessageStr);

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

    // Final cleanup for multiple &r might still be good if resets were spammed
    String finalStr = legacyResult.toString();
    finalStr = finalStr.replaceAll("(&r)+", "&r");
    // Optional: further cleanup like "&f&r" -> "&r" if &f is default reset color.

    //    System.out.println("finalStr = " + finalStr);
    return finalStr;
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
    return "f"; // Minecraft's default (white)
  }

  private static String asParam(String rawLegacyString) {
    if (rawLegacyString == null || rawLegacyString.isEmpty()) {
      return "";
    }

    String[] rawSegments = rawLegacyString.split("##", -1);

    if (rawSegments.length == 0) return "";

    // Initialize currentAccumulatedText with the MiniMessage version of the first segment
    StringBuilder resultBuilder = new StringBuilder(convertLegacyToMiniMessage(rawSegments[0]));

    List<Pair<Types, String>> activeNestableActions = new ArrayList<>();

    for (int i = 1; i < rawSegments.length; i++) {
      String currentRawSegment = rawSegments[i];

      if (currentRawSegment.isEmpty()
          && i < rawSegments.length - 1
          && rawSegments[i + 1].isEmpty()) {
        // Handle "####" case which results in an empty segment between two other empty segments if
        // they were delimiters.
        // Or if it's just "text####tag", the middle empty segment.
        // This might need more robust handling if "##" can be escaped in text.
        // For now, if it's an empty segment not at the end, and not followed by another empty one
        // (which would be from "text##")
        // we might append "##" if it's meant to be literal, or just continue.
        // Assuming "##" is purely a delimiter, an empty segment means "text####tag" -> text, "",
        // tag.
        // The "" part doesn't correspond to user text.
        continue;
      }

      String tagKey = (currentRawSegment.length() >= 4) ? currentRawSegment.substring(0, 4) : "";
      Types type = Types.getTypeFromKey(tagKey);

      if (type != null) { // It's a ##tag:value## segment
        String legacyTagValue = currentRawSegment.substring(4);

        if (isNestableActionType(type)) {
          activeNestableActions.add(new Pair<>(type, legacyTagValue));
        } else {
          // Non-nestable tag encountered, process any pending nestable actions first
          if (!activeNestableActions.isEmpty()) {
            String processedNestable =
                applyNestedActionTags(resultBuilder.toString(), activeNestableActions);
            resultBuilder.setLength(0); // Clear builder
            resultBuilder.append(processedNestable); // Append processed part
            activeNestableActions.clear();
          }
          // Apply the current non-nestable tag
          String processedNonNestable =
              performTypeValueReplacement(type.getKey(), legacyTagValue, resultBuilder.toString());
          resultBuilder.setLength(0);
          resultBuilder.append(processedNonNestable);
        }
      } else { // Segment is not a recognized tag, so it's plain text.
        // Process any pending nestable actions before appending this new text
        if (!activeNestableActions.isEmpty()) {
          String processedNestable =
              applyNestedActionTags(resultBuilder.toString(), activeNestableActions);
          resultBuilder.setLength(0);
          resultBuilder.append(processedNestable);
          activeNestableActions.clear();
        }
        // Append the MiniMessage version of this plain text segment
        // If currentRawSegment came from splitting "text1##text2", then "text2" is
        // currentRawSegment.
        // If it was "text1##", then currentRawSegment might be empty if it's the last part.
        if (!currentRawSegment.isEmpty()
            || (i == rawSegments.length - 1 && rawLegacyString.endsWith("##"))) {
          // The check for rawLegacyString.endsWith("##") handles cases like "text##" where the last
          // segment is empty
          // but represents the content after the last delimiter.
          // However, split with -1 already keeps trailing empty strings.
          // So, just convert.
          resultBuilder.append(convertLegacyToMiniMessage(currentRawSegment));
        }
      }
    }

    // Process any remaining nestable actions at the end of the string
    if (!activeNestableActions.isEmpty()) {
      String processedNestable =
          applyNestedActionTags(resultBuilder.toString(), activeNestableActions);
      resultBuilder.setLength(0);
      resultBuilder.append(processedNestable);
    }

    return resultBuilder.toString();
  }

  private static boolean isNestableActionType(Types type) {
    if (type == null) return false;
    // Define which types are considered for special nesting (e.g., click/hover)
    return switch (type) {
      case CHANGE_PAGE, COPY_TO_CLIPBOARD, OPEN_FILE, OPEN_PAGE, RUN_COMMAND, SUGGEST_COMMAND, SHOW_ENTITY, SHOW_ITEM,
           SHOW_TEXT, INSERT -> // Insert can also be part of nesting
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
    if (type == Types.SHOW_TEXT) {// Potentially SHOW_ITEM or SHOW_ENTITY if their args could be MiniMessage strings
      // (e.g. if item name in ##itm:...## could contain legacy codes)
      // For now, only SHOW_TEXT is a clear candidate.
      return true;
    }
    return false;
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
    // ##nbt:block|entity|storage:id:path[:_separator_][:interpret]##
    // Your health is ##nbt:entity:'@s':Health/##
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

    static {
      KEYS_CACHE = Arrays.stream(values()).map(Decorations::getKey).toArray(String[]::new);
    }

    Decorations(String key, String value) {
      this.key = key;
      this.value = value;
    }

    static String[] getKeys() {
      return KEYS_CACHE;
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

    static {
      KEYS_CACHE = Arrays.stream(values()).map(Colors::getKey).toArray(String[]::new);
    }

    Colors(String key, String value) {
      this.key = key;
      this.value = value;
    }

    static String[] getKeys() {
      return KEYS_CACHE;
    }

    String getKey() {
      return key;
    }

    String getValue() {
      return value;
    }
  }
}
