package com.hf.passport.text;

import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.tasks.Tasks;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;
import com.hf.passport.other.BitmapUtils;
import com.hf.passport.other.StringUtils;
import com.innovatrics.mrz.MrzParser;
import com.innovatrics.mrz.MrzRecord;
import com.innovatrics.mrz.types.MrzDate;

import org.jmrtd.lds.icao.MRZInfo;

import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class IDAnalyser {
    private static final String TAG = "IDAnalyser";
    public static final int MIN_SIZE_THRESHOLD = 4;
    private static final int MIN_CHAR_LENGTH_PER_LINE = 30;
    public static final int MIN_POSSIBLE_CHAR_LENGTH_PER_LINE = MIN_CHAR_LENGTH_PER_LINE - MIN_SIZE_THRESHOLD;
    private static final int MAX_POSSIBLE_CHAR_LENGTH_PER_LINE = 44;

    private static IDAnalyser instance;

    private final TextRecognizer gmsTextRecognizer;
    private String addressText;
    private TextRecognitionProcessor.ResultListener resultListener;
    private boolean startProcess;

    public static IDAnalyser getInstance() {
        if (null == instance) {
            instance = new IDAnalyser();
        }
        return instance;
    }

    private IDAnalyser() {
        gmsTextRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
    }

    public void setResultCallback(TextRecognitionProcessor.ResultListener resultListener) {
        this.resultListener = resultListener;
    }

    public void start() {
        startProcess = true;
    }

    public void stop() {
        startProcess = false;
    }

    public void onBitmapPrepared3(InputImage inputImage) {
        List<BlockWrapper> textBlocks = detectTextBlocks(inputImage);

        boolean detectedPossibleMrzBlock = false;
        BlockWrapper block1;
        BlockWrapper block2 = null;
        BlockWrapper block3 = null;
        String text1;
        String text2 = "";
        String text3 = "";
        String processed;
        for (int i = 0; i < textBlocks.size(); i++) {
            block1 = textBlocks.get(i);
            text1 = block1.getText();
            Log.d(TAG, "index= " + i + " block1=" + text1);
            if (isPossibleMrzBlock(text1)) {
                Log.e(TAG, "enter isPossibleMrzBlock()");
                processed = MrzTextPreProcessor.process(text1);
                Log.e(TAG, "processed=");
                try {
                    MRZInfo mrzInfo = parseMrz(processed);
                    if (null != mrzInfo && startProcess) {
                        startProcess = false;
                        resultListener.onSuccess(mrzInfo, null);

                        Bitmap bitmap = BitmapUtils.yuvToBitmap(inputImage.getByteBuffer().array(), inputImage.getWidth(), inputImage.getHeight());
                        resultListener.onBitmap(bitmap);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return;
            } else if (isPossibleSingleMrzBlock(text1)) {
                Log.e(TAG, "enter isPossibleSingleMrzBlock()");
                IDLine1 = text1;
                if (IDLine1.length() > 44) {
                    IDLine1 = IDLine1.substring(0, 44);
                } else if (IDLine1.length() < 44) {
                    IDLine1 = IDLine1 + StringUtils.repeat("<", 44 - IDLine1.length());
                }

                if (i + 1 < textBlocks.size()) {
                    block2 = textBlocks.get(i + 1);
                    text2 = block2.getText();
                }
                if (null == block2) {
                    Log.e(TAG, "leave isPossibleSingleMrzBlock()");
                    continue;
                }

                Pattern patternPassportTD3Line1 = Pattern.compile(TextRecognitionProcessor.PASSPORT_TD_3_LINE_1_REGEX);
                Matcher matcherPassportTD3Line1 = patternPassportTD3Line1.matcher(text1);

                Pattern patternPassportTD3Line2 = Pattern.compile(TextRecognitionProcessor.PASSPORT_TD_3_LINE_2_REGEX);
                Matcher matcherPassportTD3Line2 = patternPassportTD3Line2.matcher(text2);

                if (matcherPassportTD3Line1.find()) {
                    Log.e(TAG, "index= " + i + " block1=" + text1);
                }
                if (matcherPassportTD3Line2.find()) {
                    Log.e(TAG, "index= " + (i + 1) + " block2=" + text2);
                }

                if (matcherPassportTD3Line1.find() && matcherPassportTD3Line2.find() && startProcess) {
                    Log.e(TAG, "enter isPossibleMrzBlock()");
                    try {
                        processed = MrzTextPreProcessor.process(text1 + "\n" + text2);
//                        MRZInfo mrzInfo = new MRZInfo(processed);
//                        startProcess = false;
//                        resultListener.onSuccess(mrzInfo, null);
//
//                        Bitmap bitmap = BitmapUtils.yuvToBitmap(inputImage.getByteBuffer().array(), inputImage.getWidth(), inputImage.getHeight());
//                        resultListener.onBitmap(bitmap);
                        MRZInfo mrzInfo = parseMrz(processed);
                        if (null != mrzInfo && startProcess) {
                            startProcess = false;
                            resultListener.onSuccess(mrzInfo, null);

                            Bitmap bitmap = BitmapUtils.yuvToBitmap(inputImage.getByteBuffer().array(), inputImage.getWidth(), inputImage.getHeight());
                            resultListener.onBitmap(bitmap);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Parsing MRZ failed");
                    }
                    return;
                }

                Pattern patternIDCardTD1Line1 = Pattern.compile(TextRecognitionProcessor.ID_CARD_TD_1_LINE_1_REGEX);
                Matcher matcherIDCardTD1Line1 = patternIDCardTD1Line1.matcher(text1);

                Pattern patternIDCardTD1Line2 = Pattern.compile(TextRecognitionProcessor.ID_CARD_TD_1_LINE_2_REGEX);
                Matcher matcherIDCardTD1Line2 = patternIDCardTD1Line2.matcher(text2);

                if (matcherIDCardTD1Line1.find() && matcherIDCardTD1Line2.find() && startProcess) {
                    try {
                        processed = MrzTextPreProcessor.process(text1 + "\n" + text2);
                        if (i + 2 < textBlocks.size()) {
                            block3 = textBlocks.get(i + 2);
                            text3 = block3.getText();
                            processed = MrzTextPreProcessor.process(text1 + "\n" + text2 + "\n" + text3);
                        }
//                        MRZInfo mrzInfo = new MRZInfo(processed);
//                        startProcess = false;
//                        resultListener.onSuccess(mrzInfo, null);
//
//                        Bitmap bitmap = BitmapUtils.yuvToBitmap(inputImage.getByteBuffer().array(), inputImage.getWidth(), inputImage.getHeight());
//                        resultListener.onBitmap(bitmap);

                        MRZInfo mrzInfo = parseMrz(processed);
                        if (null != mrzInfo && startProcess) {
                            startProcess = false;
                            resultListener.onSuccess(mrzInfo, null);

                            Bitmap bitmap = BitmapUtils.yuvToBitmap(inputImage.getByteBuffer().array(), inputImage.getWidth(), inputImage.getHeight());
                            resultListener.onBitmap(bitmap);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Parsing MRZ failed");
                    }
                    return;
                }
            } else if (isPossibleSingleIDLine1(text1)) {
                IDLine1 = text1.replaceAll(" ", "");
                if (IDLine1.length() > 44) {
                    IDLine1 = IDLine1.substring(0, 44);
                } else if (IDLine1.length() < 44) {
                    IDLine1 = IDLine1 + StringUtils.repeat("<", 44 - IDLine1.length());
                }
                Log.e(TAG, "IDLine1=" + IDLine1);
            } else if (isPossibleSingleIDLine2(text1)) {
                IDLine2 = text1.replaceAll(" ", "");
                Log.e(TAG, "IDLine2=" + IDLine2);
                if (!TextUtils.isEmpty(IDLine1) && !TextUtils.isEmpty(IDLine2) && startProcess) {
                    try {
                        processed = MrzTextPreProcessor.process(IDLine1 + "\n" + IDLine2);
                        if (i + 2 < textBlocks.size()) {
                            block3 = textBlocks.get(i + 2);
                            text3 = block3.getText();
                            processed = MrzTextPreProcessor.process(IDLine1 + "\n" + IDLine2 + "\n" + text3);
                        }
//                        MRZInfo mrzInfo = new MRZInfo(processed);
//                        startProcess = false;
//                        resultListener.onSuccess(mrzInfo, null);
//
//                        Bitmap bitmap = BitmapUtils.yuvToBitmap(inputImage.getByteBuffer().array(), inputImage.getWidth(), inputImage.getHeight());
//                        resultListener.onBitmap(bitmap);
                        MRZInfo mrzInfo = parseMrz(processed);
                        if (null != mrzInfo && startProcess) {
                            startProcess = false;
                            resultListener.onSuccess(mrzInfo, null);

                            Bitmap bitmap = BitmapUtils.yuvToBitmap(inputImage.getByteBuffer().array(), inputImage.getWidth(), inputImage.getHeight());
                            resultListener.onBitmap(bitmap);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Parsing MRZ failed");
                    }
                    IDLine1 = null;
                    IDLine2 = null;
                    return;
                }
            }
        }
    }

    private String IDLine1;
    private String IDLine2;

    public void onBitmapPrepared2(InputImage inputImage) {
        List<BlockWrapper> textBlocks = detectTextBlocks(inputImage);

        boolean detectedPossibleMrzBlock = false;
        BlockWrapper block1;
        BlockWrapper block2 = null;
        BlockWrapper block3 = null;
        for (int i = 0; i < textBlocks.size(); i++) {
            block1 = textBlocks.get(i);
            if (i + 1 < textBlocks.size()) {
                block2 = textBlocks.get(i + 1);
            }
            Log.d(TAG, "index= " + i + " block1=" + block1.getText());
            if (null == block2) {
                continue;
            }

            Pattern patternPassportTD3Line1 = Pattern.compile(TextRecognitionProcessor.PASSPORT_TD_3_LINE_1_REGEX);
            Matcher matcherPassportTD3Line1 = patternPassportTD3Line1.matcher(block1.getText());

            Pattern patternPassportTD3Line2 = Pattern.compile(TextRecognitionProcessor.PASSPORT_TD_3_LINE_2_REGEX);
            Matcher matcherPassportTD3Line2 = patternPassportTD3Line2.matcher(block2.getText());

            if (matcherPassportTD3Line1.find()) {
                Log.e(TAG, "index= " + i + " block1=" + block1.getText());
            } else if (block1.getText().length() > 31) {
                Log.w(TAG, "index= " + i + " block1=" + block1.getText());
            }
            if (matcherPassportTD3Line2.find()) {
                Log.e(TAG, "index= " + (i + 1) + " block2=" + block2.getText());
            }

            if (matcherPassportTD3Line1.find() && matcherPassportTD3Line2.find()) {
                Log.e(TAG, "enter isPossibleMrzBlock()");
                try {
                    String processed = MrzTextPreProcessor.process(block1.getText() + "\n" + block2.getText());
                    MRZInfo mrzInfo = new MRZInfo(processed);
                    resultListener.onSuccess(mrzInfo, null);

                    Bitmap bitmap = BitmapUtils.yuvToBitmap(inputImage.getByteBuffer().array(), inputImage.getWidth(), inputImage.getHeight());
                    resultListener.onBitmap(bitmap);

//                    try {
//                        if (parseMrz(processed)) {
//                            MRZInfo mrzInfo = new MRZInfo(processed);
//                            resultListener.onSuccess(mrzInfo, null);
////
//                            Bitmap bitmap = BitmapUtils.yuvToBitmap(inputImage.getByteBuffer().array(), inputImage.getWidth(), inputImage.getHeight());
//                            resultListener.onBitmap(bitmap);
//                            return;
//                        }
//                    } catch (Exception e) {
//                        Log.e(TAG, "Parsing MRZ failed");
//                    }

                    return;
                } catch (Exception e) {
                    Log.e(TAG, "Parsing MRZ failed");
                }
                return;
            }

            Pattern patternIDCardTD1Line1 = Pattern.compile(TextRecognitionProcessor.ID_CARD_TD_1_LINE_1_REGEX);
            Matcher matcherIDCardTD1Line1 = patternIDCardTD1Line1.matcher(block1.getText());

            Pattern patternIDCardTD1Line2 = Pattern.compile(TextRecognitionProcessor.ID_CARD_TD_1_LINE_2_REGEX);
            Matcher matcherIDCardTD1Line2 = patternIDCardTD1Line2.matcher(block2.getText());

            if (matcherIDCardTD1Line1.find() && matcherIDCardTD1Line2.find()) {
                try {
                    String processed = MrzTextPreProcessor.process(block1.getText() + "\n" + block2.getText());
                    if (i + 2 < textBlocks.size()) {
                        block3 = textBlocks.get(i + 2);
                        processed = MrzTextPreProcessor.process(block1.getText() + "\n" + block2.getText() + "\n" + block3.getText());
                    }
                    MRZInfo mrzInfo = new MRZInfo(processed);
                    resultListener.onSuccess(mrzInfo, null);

                    Bitmap bitmap = BitmapUtils.yuvToBitmap(inputImage.getByteBuffer().array(), inputImage.getWidth(), inputImage.getHeight());
                    resultListener.onBitmap(bitmap);
                } catch (Exception e) {
                    Log.e(TAG, "Parsing MRZ failed");
                }
                return;
            }
        }
    }

    private MRZInfo parseMRZInfo(String mrzString) {
        try {
            return new MRZInfo(mrzString);
        } catch (Exception e) {
            Log.e(TAG, "Parsing MRZ failed");
            return null;
        }
    }

    private List<BlockWrapper> detectTextBlocks(InputImage inputImage) {
        try {
            Text result = Tasks.await(gmsTextRecognizer.process(inputImage));
            if (!result.getText().isEmpty()) {
//                Log.d(TAG, "GMS scanned raw text: " + result.getText());
            }
            return result.getTextBlocks().stream().map(BlockWrapper::new).collect(Collectors.toList());
        } catch (Exception e) {
            Log.d(TAG, "GMS text detection failed");
        }
        return Collections.emptyList();
    }

    private List<BlockWrapper> detectTextBlocks(Bitmap bitmap) {
        InputImage inputImage = InputImage.fromBitmap(bitmap, 0);
        try {
            Text result = Tasks.await(gmsTextRecognizer.process(inputImage));
            if (!result.getText().isEmpty()) {
//                Log.d(TAG, "GMS scanned raw text: " + result.getText());
            }
            return result.getTextBlocks().stream().map(BlockWrapper::new).collect(Collectors.toList());
        } catch (Exception e) {
            Log.d(TAG, "GMS text detection failed");
        }
        return Collections.emptyList();
    }

    private void detectPossibleAddressText(BlockWrapper block) {
        List<String> lines = block.getLines();

        if (lines.size() > 1 && (lines.get(0).contains("Anschrift") || lines.get(0).contains("Adresse") || lines.get(0).contains("Address"))) {
            String addressText = lines.subList(1, lines.size()).stream().collect(Collectors.joining(", "));
            if (addressText.length() > (this.addressText != null ? this.addressText.length() : 0)) {
                this.addressText = addressText;
            }
        }
    }

    public boolean isPossibleMrzBlock(String txt) {
        int newlineCount = 0;
        int lessThanCount = 0;
        boolean hasValidLines = false;

        // 计算换行符的数量
        for (char c : txt.toCharArray()) {
            if (c == '\n') {
                newlineCount++;
            }
        }

        // 计算'<'字符的数量
        for (char c : txt.toCharArray()) {
            if (c == '<') {
                lessThanCount++;
            }
        }

        // 检查是否有至少两个长度足够的行
        String[] lines = txt.split("\n");
        for (String line : lines) {
            if (line.length() >= MIN_POSSIBLE_CHAR_LENGTH_PER_LINE) {
                hasValidLines = true;
                break;
            }
        }

        return newlineCount >= 1 && lessThanCount > 10 && hasValidLines;
    }

    public boolean isPossibleSingleMrzBlock(String txt) {
        txt = txt.replaceAll(" ", "");
        int lessThanCount = 0;
        boolean hasValidLines = false;

        // 计算'<'字符的数量
        for (char c : txt.toCharArray()) {
            if (c == '<') {
                lessThanCount++;
            }
        }

        if (txt.length() >= MIN_POSSIBLE_CHAR_LENGTH_PER_LINE) {
            hasValidLines = true;
        }

        return lessThanCount > 10 && hasValidLines;
    }

    public boolean isPossibleSingleIDLine1(String txt) {
        txt = txt.replaceAll(" ", "");
        int lessThanCount = 0;
        boolean hasValidLines = false;

        // 计算'<'字符的数量
        for (char c : txt.toCharArray()) {
            if (c == '<') {
                lessThanCount++;
            }
        }

        if (txt.length() >= 20) {
            hasValidLines = true;
        }

        Pattern patternIDCardTD1Line1 = Pattern.compile(TextRecognitionProcessor.PASSPORT_TD_3_LINE_1_REGEX_SHORT);
        Matcher matcherIDCardTD1Line1 = patternIDCardTD1Line1.matcher(txt);

        return lessThanCount >= 8 && hasValidLines && matcherIDCardTD1Line1.find();
    }

    public boolean isPossibleSingleIDLine2(String txt) {
        txt = txt.replaceAll(" ", "");
        Pattern patternPassportTD3Line2 = Pattern.compile(TextRecognitionProcessor.PASSPORT_TD_3_LINE_2_REGEX);
        Matcher matcherPassportTD3Line2 = patternPassportTD3Line2.matcher(txt);

        return matcherPassportTD3Line2.find();
    }

    public MRZInfo parseMrz(String processed) {
        Log.e(TAG, "parseMrz processed=" + processed);
        MrzRecord record = MrzParser.parse(processed);
        String documentNumber = record.documentNumber;
        String givenNames = record.givenNames;
        int index = givenNames.indexOf("<<");
        if (index != -1){
            givenNames = givenNames.substring(0, index);
        }
        index = givenNames.indexOf(", ,");
        if (index != -1){
            givenNames = givenNames.substring(0, index);
        }
        String surname = record.surname;
        MrzDate birthDate = record.dateOfBirth;
        String nationality = record.nationality;
        String gender = record.sex.name();
        String issuingCountry = record.issuingCountry;
        MrzDate expirationDate = record.expirationDate;

        boolean nameNeedCorrection = Character.isLetter(processed.charAt(processed.length() - 1)) || givenNames.contains(PARSER_FILLER_REPLACEMENT) || surname.contains(PARSER_FILLER_REPLACEMENT);

        if (record.validDocumentNumber && !givenNames.trim().isEmpty() && !surname.trim().isEmpty() && (birthDate != null && birthDate.isDateValid()) && expirationDate.isDateValid() && !nationality.trim().isEmpty()) {
            Log.e(TAG, "parseMrz success !!!!!!!!!!!!!!!!!");
//            String correctedGivenNames = givenNames.replace(PARSER_FILLER_REPLACEMENT, "");
//            String correctedSurname = surname.replace(PARSER_FILLER_REPLACEMENT, "");
            MRZInfo mrzInfo =  new MRZInfo(processed);
            mrzInfo.setSecondaryIdentifiers(givenNames);
            return mrzInfo;
        }
        Log.e(TAG, "parseMrz failure ********************");
        return null;
    }

    public void close() {
        gmsTextRecognizer.close();
    }

    private static final String PARSER_FILLER_REPLACEMENT = ", "; //mrz parser replaces all "<<" with ", " within recognized name fields
}
