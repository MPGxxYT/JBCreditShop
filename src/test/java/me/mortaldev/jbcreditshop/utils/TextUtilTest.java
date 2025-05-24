package me.mortaldev.jbcreditshop.utils;

class TextUtilTest {

  @org.junit.jupiter.api.Test
  void fileFormat() {
    String input1 = "  Hello World!  ";
    String expected1 = "Hello_World";
    String actual1 = TextUtil.fileFormat(input1);
    assert actual1.equals(expected1)
        : "Input: '" + input1 + "', Expected: '" + expected1 + "', Actual: '" + actual1 + "'";

    String input2 = "---Another_Test---";
    String expected2 = "Another_Test";
    String actual2 = TextUtil.fileFormat(input2);
    assert actual2.equals(expected2)
        : "Input: '" + input2 + "', Expected: '" + expected2 + "', Actual: '" + actual2 + "'";

    String input3 = "Special@#$Characters";
    String expected3 = "Special_Characters";
    String actual3 = TextUtil.fileFormat(input3);
    assert actual3.equals(expected3)
        : "Input: '" + input3 + "', Expected: '" + expected3 + "', Actual: '" + actual3 + "'";

    String input4 = "  .Leading.and.Trailing.  ";
    String expected4 = "Leading_and_Trailing";
    String actual4 = TextUtil.fileFormat(input4);
    assert actual4.equals(expected4)
        : "Input: '" + input4 + "', Expected: '" + expected4 + "', Actual: '" + actual4 + "'";

    String input5 = "NoSpecialChars";
    String expected5 = "NoSpecialChars";
    String actual5 = TextUtil.fileFormat(input5);
    assert actual5.equals(expected5)
        : "Input: '" + input5 + "', Expected: '" + expected5 + "', Actual: '" + actual5 + "'";

    String input6 = "   ";
    String expected6 = "formatted_string"; // Based on the current implementation's default
    String actual6 = TextUtil.fileFormat(input6);
    assert actual6.equals(expected6)
        : "Input: '" + input6 + "', Expected: '" + expected6 + "', Actual: '" + actual6 + "'";

    String input7 = "!!!";
    String expected7 = "formatted_string"; // Based on the current implementation's default
    String actual7 = TextUtil.fileFormat(input7);
    assert actual7.equals(expected7)
        : "Input: '" + input7 + "', Expected: '" + expected7 + "', Actual: '" + actual7 + "'";
  }
}
