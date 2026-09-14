package SMS;

import java.util.ArrayDeque;

/**
 * Converts between raw PDU bytes and their hexadecimal text form.
 *
 * The network layers move messages around as strings, because that is how a
 * PDU is normally written down and shown to a person, while the encoding
 * itself works on bytes.
 */
public class HEX {

    /** Writes every byte as two hexadecimal digits, high half byte first. */
    public static String toHexadecimal(byte[] arr) {
        var sb = new StringBuilder();
        for (int i = 0; i < arr.length; i++) {
            int number = arr[i];
            var res = "";
            for(int j = 0; j < 2; j++){
                String s = String.valueOf(number & 0b1111) 
                    .replace("10", "A")
                    .replace("11", "B")
                    .replace("12", "C")
                    .replace("13", "D")
                    .replace("14", "E")
                    .replace("15", "F");
                res = s + res;
                number = number >> 4;
            }
            sb.append(res);
        }
        return sb.toString();
    }

    /** Reads back the bytes of a string produced by {@link #toHexadecimal}. */
    public static byte[] fromHexadecimal(String str) {
        var size = (str.length() / 2);
        var res = new byte[size];

        var nums = new ArrayDeque<Integer>();
        for (var c : str.toCharArray()) {
            var s = (c + "")
                .replace("A", "10")
                .replace("B", "11")
                .replace("C", "12")
                .replace("D", "13")
                .replace("E", "14")
                .replace("F", "15");
            nums.offer(Integer.parseInt(s));
        }

        for (int i = 0; i < res.length; i++) {
            long n1 = nums.poll();
            n1= n1 << 4;
            long n2 = 0;
            if (nums.size() > 0) {
                n2 = nums.poll();
            }
            res[i] = (byte)(n1 | n2);
        }

        return res;
    }
}
