package org.onepf.opfpush.testutil;

import org.onepf.opfpush.mock.MockNamePushProvider;
import org.onepf.opfpush.pushprovider.PushProvider;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * @author antonpp
 * @since 13.04.15
 */
public final class Util {

    public static final int NUM_TESTS = 100;
    public static final int NUM_PROVIDERS = 100;
    public static final int RANDOM_STRING_LENGTH = 16;
    private static final Random RND = new Random();

    private Util() {
        throw new UnsupportedOperationException();
    }

    public static List<String> shuffleStringArray(final String[] array) {
        final List<String> mixedArray = Arrays.asList(array);
        Collections.shuffle(mixedArray);
        return mixedArray;
    }

    public static String[] getRandomStrings(int n, int len) {
        char[] chars = "abcdefghijklmnopqrstuvwxyz0123456789".toCharArray();
        String[] strings = new String[n];
        for (int i = 0; i < n; ++i) {
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < len; j++) {
                char c = chars[RND.nextInt(chars.length)];
                sb.append(c);
            }
            strings[i] = sb.toString();
        }
        return strings;
    }

    public static PushProvider[] getRandomPushProviders() {
        final MockNamePushProvider[] pushProviders = new MockNamePushProvider[NUM_PROVIDERS];
        for (int i = 0; i < NUM_PROVIDERS; ++i) {
            pushProviders[i] = new MockNamePushProvider(String.format("provider%d", i + 1));
        }
        return pushProviders;
    }
}
