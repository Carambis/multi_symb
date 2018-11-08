package com.bsuir;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static final int COUNT_LEARN = 10;
    private static final int SIZE_IMAGE = 9900;
    private static final int NUMBER_SET = 3;
    private static int[] weights = new int[SIZE_IMAGE];
    private static final int BIAS = 5;
    private static final String PATH = "src\\main\\resources\\study_file\\";
    private static final String PATH_TO_WEIGHTS = "src\\main\\resources\\weights.txt";
    private static final String PATH_TEST = "src\\main\\resources\\test_file\\";
    private static final String PNG = ".PNG";
    private static final int TEST_NUMBER = 2;
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(PATH_TO_WEIGHTS))) {
            String str;
            int i = 0;
            while ((str = reader.readLine()) != null) {
                weights[i] = Integer.parseInt(str);
                i++;
            }
            if (i != SIZE_IMAGE) {
                learn();
            }
        } catch (FileNotFoundException e) {
            learn();
        }

        while (true) {
            try {
                test();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static boolean proceed(int[] number) {
        int net = 0;
        for (int i = 0; i < number.length; i++) {
            net += number[i] * weights[i];
        }
        return net >= BIAS;
    }

    private static void decrease(int[] number) {
        for (int i = 0; i < number.length; i++) {
            if (number[i] == 1) {
                weights[i]--;
            }
        }
    }

    private static void increase(int[] number) {
        for (int i = 0; i < number.length; i++) {
            if (number[i] == 1) {
                weights[i]++;
            }
        }
    }

    private static int[] readImage(String fileName) throws IOException {
        BufferedImage image = ImageIO.read(new File(fileName));
        int[][] arrays = getBW(image);
        arrays = renderImage(arrays);

        BufferedImage bi = new BufferedImage(arrays[0].length, arrays.length, BufferedImage.TYPE_INT_RGB);
        for (int i = 0; i < arrays.length; i++) {
            for (int j = 0; j < arrays[i].length; j++) {
                if (arrays[i][j] == 1) {
                    bi.setRGB(j, i, 0);
                } else {
                    bi.setRGB(j, i, 1);
                }
            }
        }

        BufferedImage scaled = new BufferedImage(90, 110,
                BufferedImage.TYPE_INT_RGB);
        Graphics2D g = scaled.createGraphics();
        g.drawImage(bi, 0, 0, 90, 110, null);
        g.dispose();

        arrays = getBW(scaled);

        int[] points = new int[arrays.length * arrays[0].length];
        int k = 0;
        for (int[] array : arrays) {
            for (int anArray : array) {
                points[k] = anArray;
                k++;
            }
        }

        return points;
    }

    private static void learn() throws IOException {
        System.out.println("Start learning");
        List<int[]> list = new ArrayList<>();
        for (int i = 0; i < COUNT_LEARN; i++) {
            for (int j = 0; j < NUMBER_SET; j++) {
                list.add(readImage(PATH + i + "" + j + PNG));
            }
        }

        for (int i = 0; i < 10000; i++) {
            for (int j = 0; j < list.size(); j++) {
                int[] number = list.get(j);
                if (j / NUMBER_SET != TEST_NUMBER) {
                    if (proceed(number)) {
                        decrease(number);
                    }
                } else {
                    if (!proceed(number)) {
                        increase(number);
                    }
                }
            }
        }

        FileWriter fileWriter = new FileWriter(new File(PATH_TO_WEIGHTS));
        for (int weight : weights) {
            fileWriter.append(String.valueOf(weight));
            fileWriter.append("\n");
        }
        fileWriter.flush();
        fileWriter.close();

        System.out.println("Finish");
    }

    private static void test() throws IOException {
        System.out.println("Input number");
        String string = scanner.next();

        switch (string) {
            case "2":
            case "5":
            case "32":
            case "90":
                System.out.println(string + " = " + proceed(readImage(PATH_TEST + string + PNG)));
                break;
            default:
                System.out.println("Incorrect number");
        }
    }

    private static int[][] renderImage(int[][] image) {
        while (checkRow(image, 0)) {
            deleteFirstRow(image);
        }
        while (checkRow(image, image.length - 1)) {
            image = deleteLastRow(image);
        }
        while (checkColumn(image, 0)) {
            deleteLeftColumn(image);
        }
        while (checkColumn(image, image[0].length - 1)) {
            image = deleteRightColumn(image);
        }
        return image;
    }

    private static boolean checkRow(int[][] image, int row) {
        int width = image[row].length;
        for (int i = 0; i < width; i++) {
            if (image[row][i] == 1) {
                return false;
            }
        }
        return true;
    }

    private static boolean checkColumn(int[][] image, int column) {
        for (int[] anImage : image) {
            if (anImage[column] == 1) {
                return false;
            }
        }
        return true;
    }

    private static int[][] deleteFirstRow(int[][] image) {
        if (image.length - 1 >= 0) {
            System.arraycopy(image, 1, image, 0, image.length - 1);
        }
        return image;
    }

    private static int[][] deleteLastRow(int[][] image) {
        int[][] newImage = new int[image.length - 1][image[0].length];
        System.arraycopy(image, 0, newImage, 0, newImage.length);
        return newImage;
    }

    private static int[][] deleteLeftColumn(int[][] image) {
        for (int[] anImage : image) {
            if (image[0].length - 1 >= 0) {
                System.arraycopy(anImage, 1, anImage, 0, image[0].length - 1);
            }
        }
        return image;
    }

    private static int[][] deleteRightColumn(int[][] image) {
        int[][] newImage = new int[image.length][image[0].length - 1];
        for (int i = 0; i < newImage.length; i++) {
            if (newImage[0].length - 1 >= 0) {
                System.arraycopy(image[i], 0, newImage[i], 0, newImage[0].length);
            }
        }
        return newImage;
    }

    private static int[][] getBW(BufferedImage image) {
        int height = image.getHeight();
        int width = image.getWidth();
        int[][] arrays = new int[height][width];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int rgb = image.getRGB(j, i);
                Color color = new Color((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF);
                arrays[i][j] = color.equals(Color.BLACK) ? 1 : 0;
            }
        }
        return arrays;
    }
}
