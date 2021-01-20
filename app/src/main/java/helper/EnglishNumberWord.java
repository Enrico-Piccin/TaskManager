package helper;

public class EnglishNumberWord {
    private static final String[] tensNames = { "", "ten", "twenty", "thirty" };

    private static final String[] numNames = { "", "one", "two", "three", "four", "five",
            "six", "seven", "eight", "nine", "ten", "eleven", "twelve", "thirteen",
            "fourteen", "fifteen", "sixteen", "seventeen", "eighteen", "nineteen" };

    public static String convert(int number) {
        String word;

        if (number % 100 < 20)
        {
            word = numNames[number % 100];
            number /= 100;
        } else
        {
            word = numNames[number % 10];
            number /= 10;

            word = tensNames[number % 10] + word;
            number /= 10;
        }
        return word;
    }


}
