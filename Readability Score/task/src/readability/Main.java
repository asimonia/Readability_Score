package readability;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {

    private static List<String> allLines;
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {

        String fileName = args[0];

        try {
            allLines = Files.readAllLines(Paths.get(fileName));
            System.out.println("The text is:");
            for (String line : allLines) {
                System.out.println(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<String[]> sentences = new ArrayList<>();

        for (String line : allLines) {
            sentences.add(line.split("(?<=[\\.\\?\\!])\\s"));
        }

        List<String[]> words = new ArrayList<>();

        for (String[] line : sentences) {
            for (String l : line) {
                words.add(Arrays.stream(l.split("\\s+"))
                                .filter(s -> !s.isEmpty())
                                .toArray(String[]::new));
            }
        }

        int numSentences = words.size();
        int numWords = 0;
        int numChars = 0;
        int syllables = 0;
        int polysyllables = 0;

        for (String[] line : words) {
            numWords += line.length;
            for (String word : line) {
                numChars += word.length();
                syllables += countSyllables(word);
                if (isPolysyllable(word))
                    polysyllables++;
            }
        }

        System.out.format("\nWords: %d\n", numWords);
        System.out.format("Sentences: %d\n", numSentences);
        System.out.format("Characters: %d\n", numChars);
        System.out.format("Syllables: %d\n", syllables);
        System.out.format("Polysyllables: %d\n", polysyllables);
        System.out.print("Enter the score you want to calculate (ARI, FK, SMOG, CL, all): ");
        String score = scanner.nextLine();
        System.out.println("");

        double t = ARI(numChars, numWords, numSentences);
        double u = fleschKincaid(numWords, numSentences, syllables);
        double v = smog(numSentences, polysyllables);
        double w = coleman(numChars, numSentences, numWords);
        double avgAge = (age(t) + age(u) + age(v) + age(w)) / 4.00;

        switch (score) {
            case "ARI":
                System.out.println("Automated Readability Index: " + calculate(t) + " (about " + age(t) + " year olds).");
                break;
            case "FK":
                System.out.println("Flesch–Kincaid readability tests: " + calculate(u) + " (about " + age(u) + " year olds).");
                break;
            case "SMOG":
                System.out.println("Simple Measure of Gobbledygook: " + calculate(v) + " (about " + age(v) + " year olds).");
                break;
            case "CL":
                System.out.println("Coleman–Liau index: " + calculate(w) + " (about " + age(w) + " year olds).");
                break;
            case "all":
                System.out.println("Automated Readability Index: " + calculate(t) + " (about " + age(t) + " year olds).");
                System.out.println("Flesch–Kincaid readability tests: " + calculate(u) + " (about " + age(u) + " year olds).");
                System.out.println("Simple Measure of Gobbledygook: " + calculate(v) + " (about " + age(v) + " year olds).");
                System.out.println("Coleman–Liau index: " + calculate(w) + " (about " + age(w) + " year olds).");
                break;
        }

        System.out.println("This text should be understood in average by " + calculate(avgAge) +  " year olds.");
    }


    private static int countSyllables(String word) {
        /*
        1. Count the number of vowels in the word.
        2. Do not count double-vowels (for example, "rain" has 2 vowels but is only 1 syllable)
        3. If the last letter in the word is 'e' do not count it as a vowel (for example, "side" is 1 syllable)
        4. If at the end it turns out that the word contains 0 vowels, then consider this word as 1-syllable.
         */

        word = word.toLowerCase().replaceAll("[\\.\\!\\,]", "");

        if (word.endsWith("e")) {
            word = word.substring(0, word.length() - 1);
        }

        Pattern vowelPattern = Pattern.compile("[aeiouy]{1,2}");
        Matcher matcher = vowelPattern.matcher(word);

        int count = 0;
        while (matcher.find()) {
            count++;
        }
        return count == 0 ? 1 : count;
    }

    private static boolean isPolysyllable(String word) {
        return countSyllables(word) > 2;
    }

    private static double fleschKincaid(int words, int sentences, int syllables) {
        return 0.39 * ((double) words / (double)sentences) +
                        11.8 * ((double) syllables / (double)words) - 15.59;

    }

    private static double smog(int sentences, int polysyllables) {
        return 1.043 * Math.sqrt((double) polysyllables * (30.0 / (double) sentences)) + 3.1291;
    }

    private static double coleman(int numChars, int sentences, int numWords) {
        double L = ((double) numChars / (double) numWords) * 100;
        double S = ((double) sentences / (double) numWords) * 100;

        return 0.0588 * L - 0.296 * S - 15.8;
    }

    private static double ARI(int numChars, int numWords, int numSentences) {
        return (4.71 * ((double)numChars / (double)numWords) +
                0.5 * ((double)numWords / (double)numSentences)) - 21.43;
    }

    private static BigDecimal calculate(double num) {
        return BigDecimal.valueOf(num).setScale(2, RoundingMode.FLOOR);
    }

    private static int age(double n) {
        int score = (int) (Math.ceil(n));
        int[] age = {0, 6,7,9,10,11,12,13,14,15,16,17,18,24,24};
        if (score >= 15) {
            return 24;
        } else {
            return age[score];
        }
    }
}
