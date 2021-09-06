package com.masum.pdfdownloader.others;

import java.util.Locale;

public class Utils {

    public static String getBytesToMBString(long bytes){
        return String.format(Locale.ENGLISH, "%.2f", bytes / (1024.00 * 1024.00));
    }
}
