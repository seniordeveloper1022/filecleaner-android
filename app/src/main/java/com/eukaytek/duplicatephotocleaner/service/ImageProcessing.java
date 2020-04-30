package com.eukaytek.duplicatephotocleaner.service;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;

import java.util.Arrays;

public class ImageProcessing {

    @SuppressWarnings("WeakerAccess")
    public static Bitmap lastBitmapA;
    public static int WaveletCoefsNumber = 200;
    private static int pixelCount = 128 * 128;
    private static double waveletMatixA[][] = new double[128][128];
    private static double waveletMatixB[][] = new double[128][128];
    private static byte[] pixelsA = new byte[pixelCount];
    private static byte[] pixelsB = new byte[pixelCount];
    private static double decompositionArray[] = new double[128];
    private static double coeff[] = new double[16384];

    private static Bitmap convertToGreyScale(Bitmap bitmap, byte[] imageBytes) {
        int h = bitmap.getHeight();
        int w = bitmap.getWidth();
        int pixels[] = new int[h * w];
        bitmap.getPixels(pixels, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        for (int j = h - 1; j >= 0; j--) {
            for (int i = w - 1; i >= 0; i--) {
                int p = pixels[i + (j * w)];
                float greyColor = 0.3f * Color.red(p) + 0.59f * Color.green(p) + 0.11f * Color.blue(p);
                imageBytes[i + j * w] = Float.valueOf(greyColor).byteValue();//Float.valueOf(greyColor).intValue();
            }
        }
        return null;
    }

    private static void BlurGreyMedian(byte[] pixels) {
        int h = 128;
        int w = 128;
        Byte arr[] = new Byte[9];
        for (int j = h - 2; j > 0; j--) {
            for (int i = w - 2; i > 0; i--) {
                arr[0] = pixels[(i - 1) + ((j - 1) * w)];
                arr[1] = pixels[i + ((j - 1) * w)];
                arr[2] = pixels[(i + 1) + ((j - 1) * w)];

                arr[3] = pixels[(i - 1) + (j * w)];
                arr[4] = pixels[i + (j * w)];
                arr[5] = pixels[(i + 1) + (j * w)];

                arr[6] = pixels[(i - 1) + ((j + 1) * w)];
                arr[7] = pixels[i + ((j + 1) * w)];
                arr[8] = pixels[(i + 1) + ((j + 1) * w)];

                Arrays.sort(arr);
                //arrays.sort sorts in increasing order
                pixels[i + (j * w)] = arr[4];
            }
        }
    }

    private static int getAverageColor(byte[] pixels) {
        int h = 128;
        int w = 128;
        int red = 0;
        for (int j = h - 1; j >= 0; j--) {
            for (int i = w - 1; i >= 0; i--) {
                red += Byte.valueOf(pixels[i + j * w]).intValue();
            }
        }
        //int returnValue = Long.valueOf(red / pixels.length).intValue();
        //Timber.e("Average color value = %d", red/pixels.length);
        return red / pixels.length;
    }

    private static void decomposeArray(double waveArray[]) {
        int h = 128;
        for (int i = 0; i < 128; i++) {
            waveArray[i] = waveArray[i] / Math.sqrt(h);
        }
        while (h > 1) {
            h = h / 2;
            for (int i = 0; i < h; i++) {
                decompositionArray[i] = (waveArray[2 * i] + waveArray[(2 * i) + 1]) / Math.sqrt(2);
                decompositionArray[h + i] = (waveArray[2 * i] - waveArray[(2 * i) + 1]) / Math.sqrt(2);
            }
            System.arraycopy(decompositionArray, 0, waveArray, 0, waveArray.length);
        }
        Arrays.fill(decompositionArray, 0);
    }

    private static void decomposeImage(double imageArray[][]) {

        //decompose rows
        for (int i = 0; i < 128; i++) {
            decomposeArray(imageArray[i]);
        }

        //perform a matrix transpose
        for (int i = 0; i < 128; i++) {
            for (int j = i; j < 128; j++) {
                if (i == j)
                    continue;

                double temp = imageArray[i][j];
                imageArray[i][j] = imageArray[j][i];
                imageArray[j][i] = temp;
            }
        }


        //decompose col
        for (int i = 0; i < 128; i++) {
            decomposeArray(imageArray[i]);
        }

        // transpose back matrix
        for (int i = 0; i < 128; i++) {
            for (int j = i; j < 128; j++) {
                if (i == j)
                    continue;

                double temp = imageArray[i][j];
                imageArray[i][j] = imageArray[j][i];
                imageArray[j][i] = temp;
            }
        }
    }

    private static void hideUnsignedCoeff(double[][] waveLetArray, Integer k) {
        int l = 0;
        k = 0;
        for (int i = 0; i < 128; i++) {
            for (int j = 0; j < 128; j++) {
                if (i == 0 && j == 0) {
                    coeff[k] = 0;
                } else {
                    coeff[k] = Math.abs(waveLetArray[i][j]);
                }
                k++;
            }
        }
        Arrays.sort(coeff);
        k = 0;
        for (int i = 0; i < 128; i++) {
            for (int j = 0; j < 128; j++) {
                if (Math.abs(waveLetArray[i][j]) > coeff[16383 - WaveletCoefsNumber] && (i != 0 || j != 0)) {
                    if (waveLetArray[i][j] > 0) {
                        waveLetArray[i][j] = 1;
                    } else {
                        waveLetArray[i][j] = -1;
                    }
                    l++;
                } else {
                    waveLetArray[i][j] = 0;
                }
            }
        }
        k = k + l;
    }

    private static void getWaveLetMatrix(byte[] pixels, double WaveletCoefs[][], int averageColor) {
        Integer k = 0;
        int w = 128;
        int h = 128;
        for (int j = 0; j < h; j++) {
            for (int i = 0; i < w; i++) {
                WaveletCoefs[j][i] = pixels[i + j * w] - averageColor;
            }
        }

        decomposeImage(WaveletCoefs);
        hideUnsignedCoeff(WaveletCoefs, k);
    }

    private static void getWaveletCoeffs(double waveletCoeff[][], TWaveletPoint positiveCoeff[], TWaveletPoint negativeCoeff[]) {
        int k_pos, k_neg;
        for (int i = 0; i < WaveletCoefsNumber; i++) {
            positiveCoeff[i].X = 0;
            positiveCoeff[i].Y = 0;

            negativeCoeff[i].X = 0;
            negativeCoeff[i].Y = 0;
        }

        k_neg = 0;
        k_pos = 0;

        for (int i = 0; i < 128; i++) {
            for (int j = 0; j < 128; j++) {
                if (waveletCoeff[i][j] > 0 && k_pos < WaveletCoefsNumber) {
                    positiveCoeff[k_pos].X = i;
                    positiveCoeff[k_pos].Y = j;
                    k_pos++;
                } else if (waveletCoeff[i][j] < 0 && k_neg < WaveletCoefsNumber) {
                    negativeCoeff[k_neg].X = i;
                    negativeCoeff[k_neg].Y = j;
                    k_neg++;
                }
            }
        }
    }

    public static double getSimilarity(double waveletCoeffA[][], TWaveletPoint postiveCoeffB[],
                                       TWaveletPoint negativeCoeffB[]) {
        double result = 0;

        for (int k = 0; k < WaveletCoefsNumber; k++) {
            if (waveletCoeffA[postiveCoeffB[k].X][postiveCoeffB[k].Y] > 0 &&
                    (postiveCoeffB[k].X > 0 || postiveCoeffB[k].Y > 0)) {
                result += 1;
            }
        }

        for (int k = 0; k < WaveletCoefsNumber; k++) {
            if (waveletCoeffA[negativeCoeffB[k].X][negativeCoeffB[k].Y] < 0 &&
                    (negativeCoeffB[k].X > 0 || negativeCoeffB[k].Y > 0)) {
                result += 1;
            }
        }
        //Log.e("SimilaritResult", " Similarity = " + result);
        result = (result * 100) / WaveletCoefsNumber;
        return result;
    }

    public static void getMatricesForBitmap(String bitmapPath, double waveLetCoeff[][], TWaveletPoint positiveCoeff[], TWaveletPoint negativeCoeff[]) {
        Bitmap bb = BitmapFactory.decodeFile(bitmapPath);

        if (bb == null) {
            return;
        }

        Bitmap b = Bitmap.createScaledBitmap(bb, 128, 128, false);
        //step 1 : Convert to grey scale
        convertToGreyScale(b, pixelsB);
        //step 2: apply blurring
        BlurGreyMedian(pixelsB);

        int averageColorB = getAverageColor(pixelsB);

        getWaveLetMatrix(pixelsB, waveLetCoeff, averageColorB);

        for (int i = 0; i < WaveletCoefsNumber; i++) {
            positiveCoeff[i] = new TWaveletPoint();
            negativeCoeff[i] = new TWaveletPoint();
        }

        getWaveletCoeffs(waveLetCoeff, positiveCoeff, negativeCoeff);
    }

    private static double getImageSimilarity(String imagePathA, String imagePathB) {
        if (lastBitmapA == null) {
            Bitmap a = BitmapFactory.decodeFile(imagePathA);
            if (a == null) {
                return 0;   //failed to decode file
            }
            Bitmap aa = Bitmap.createScaledBitmap(a, 128, 128, false);
            lastBitmapA = convertToGreyScale(aa, pixelsA);
//            StringBuilder builder = new StringBuilder(100000);
//            for(int i = 0; i < pixelsA.length; i++) {
//                builder.append(pixelsA[i]);
//            }
//            Timber.e("PixelsA = %s", builder.toString());
            BlurGreyMedian(pixelsA);
            int averageColorA = getAverageColor(pixelsA);
            getWaveLetMatrix(pixelsA, waveletMatixA, averageColorA);
        }

        Bitmap bb = BitmapFactory.decodeFile(imagePathB);

        if (bb == null) {
            return 0;   //failed to decode file
        }

        Bitmap b = Bitmap.createScaledBitmap(bb, 128, 128, false);

        //step 1 : Convert to grey scale
        convertToGreyScale(b, pixelsB);
        //step 2: apply blurring
        BlurGreyMedian(pixelsB);

        int averageColorB = getAverageColor(pixelsB);

        getWaveLetMatrix(pixelsB, waveletMatixB, averageColorB);

        TWaveletPoint positiveCoeffB[] = new TWaveletPoint[WaveletCoefsNumber];
        TWaveletPoint negativeCoeffB[] = new TWaveletPoint[WaveletCoefsNumber];

        for (int i = 0; i < WaveletCoefsNumber; i++) {
            positiveCoeffB[i] = new TWaveletPoint();
            negativeCoeffB[i] = new TWaveletPoint();
        }

        getWaveletCoeffs(waveletMatixB, positiveCoeffB, negativeCoeffB);

        return getSimilarity(waveletMatixA, positiveCoeffB, negativeCoeffB);
    }

    private static void dumpMatrix(String matrix, double dd[][]) {
        StringBuilder buffer = new StringBuilder(1000000);
        int counter = 0;
        buffer.append(matrix).append(" -> ");
        for (double[] aDd : dd) {
            buffer.append("[").append(String.valueOf(counter)).append("]");
            for (double anADd : aDd) {
                buffer.append(String.valueOf(anADd));
                buffer.append(" ");
                counter++;
            }
            buffer.append("\n");
            buffer.setLength(0);
            counter++;
        }
    }

    public static class TWaveletPoint {
        int X;
        int Y;

        TWaveletPoint() {
            X = 0;
            Y = 0;
        }
    }
}