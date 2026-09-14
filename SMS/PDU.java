package SMS;

import java.nio.ByteBuffer;
import java.time.ZonedDateTime;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

/**
 * Builds and reads SMS messages in the PDU format described by the GSM 03.38
 * and 03.40 standards.
 *
 * A PDU is a compact byte structure that a real phone sends to the network.
 * It starts with the service centre address, continues with a few header
 * octets and a timestamp, and ends with the text itself. The constants below
 * are the single bits and bit groups of those header octets, named after the
 * standard so that they can be looked up there.
 */
public class PDU {

    public static final int TP_0_MTI_SMS_DELIVER      = 0b00000000;
    public static final int TP_1_MMS_MORE_MESSAGES    = 0b00000000;
    public static final int TP_1_MMS_NO_MORE_MESSAGES = 0b00000100;
    public static final int TP_5_SRI_VAL              = 0b00000000;
    public static final int TP_6_UDHI_VAL             = 0b00000000;
    public static final int TP_7_RP_VAL               = 0b00000000;

    public static final int TP_PID_7_6_VAL            = 0b00000000;
    public static final int TP_PID_5_VAL              = 0b00000000;
    public static final int TP_PID_4_0_IMPLICIT       = 0b00000000;
    public static final int TP_PID_4_0_TELEX          = 0b00000001;
    public static final int TP_PID_4_0_TELEFAX        = 0b00000010;
    public static final int TP_PID_4_0_VOICE          = 0b00000100;
    public static final int TP_PID_4_0_ERMES          = 0b00000101;
    public static final int TP_PID_4_0_NAT_PAG_SYS    = 0b00000110;
    public static final int TP_PID_4_0_X_400          = 0b00010001;
    public static final int TP_PID_4_0_INTERNET       = 0b00010010;

    public static final int TP_DSC_7_6_VAL            = 0b00000000;
    public static final int TP_DSC_5_UNCOMPRESSED     = 0b00000000;
    public static final int TP_DSC_5_COMPRESSED       = 0b00100000;
    public static final int TP_DSC_4_NO_MESSAGE       = 0b00000000;
    public static final int TP_DSC_4_MESSAGE          = 0b00010000;
    public static final int TP_DSC_3_2_DEFAULT        = 0b00000000;
    public static final int TP_DSC_3_2_8_BIT          = 0b00000100;
    public static final int TP_DSC_3_2_UCS_2          = 0b00000010;
    public static final int TP_DSC_3_2_RESERVED       = 0b00000011;
    public static final int TP_DSC_1_0_CLASS_0        = 0b00000000;
    public static final int TP_DSC_1_0_CLASS_1        = 0b00000001;
    public static final int TP_DSC_1_0_CLASS_2        = 0b00000010;
    public static final int TP_DSC_1_0_CLASS_3        = 0b00000011;

    public static final int SMSC_TYPE_7_VAL             = 0b10000000;
    public static final int SMSC_TYPE_6_4_UNKNOWN       = 0b00000000;
    public static final int SMSC_TYPE_6_4_INTERNATIONAL = 0b00010000;
    public static final int SMSC_TYPE_6_4_NATIONAL      = 0b00100000;
    public static final int SMSC_TYPE_6_4_RESERVED      = 0b01110000;
    public static final int SMSC_TYPE_3_0_UNKNOWN       = 0b00000000;
    public static final int SMSC_TYPE_3_0_ISDN          = 0b00000001;
    public static final int SMSC_TYPE_3_0_RESERVED      = 0b00001111;

    /** The GSM 7-bit default alphabet: one code per supported character. */
    private static final Map<Character, Byte> charactersMap = new HashMap<>() {{
        put('@',      (byte)0x00); put('£',      (byte)0x01); put('$',      (byte)0x02); put('¥',      (byte)0x03);
        put('è',      (byte)0x04); put('é',      (byte)0x05); put('ù',      (byte)0x06); put('ì',      (byte)0x07);
        put('ò',      (byte)0x08); put('Ç',      (byte)0x09); put('\n',     (byte)0x0a); put('Ø',      (byte)0x0b);
        put('ø',      (byte)0x0c); put('\r',     (byte)0x0d); put('Å',      (byte)0x0e); put('å',      (byte)0x0f);
        put('\u0394', (byte)0x10); put('_',      (byte)0x11); put('\u03A6', (byte)0x12); put('\u0393', (byte)0x13);
        put('\u039B', (byte)0x14); put('\u03A9', (byte)0x15); put('\u03A0', (byte)0x16); put('\u03A8', (byte)0x17);
        put('\u03A3', (byte)0x18); put('\u0398', (byte)0x19); put('\u039E', (byte)0x1a); put('\u001B', (byte)0x1b);
        put('Æ',      (byte)0x1c); put('æ',      (byte)0x1d); put('ß',      (byte)0x1e); put('É',      (byte)0x1f);
        put('\u0020', (byte)0x20); put('!',      (byte)0x21); put('\"',     (byte)0x22); put('#',      (byte)0x23);
        put('¤',      (byte)0x24); put('%',      (byte)0x25); put('&',      (byte)0x26); put('\'',     (byte)0x27);
        put('(',      (byte)0x28); put(')',      (byte)0x29); put('*',      (byte)0x2a); put('+',      (byte)0x2b);
        put(',',      (byte)0x2c); put('-',      (byte)0x2d); put('.',      (byte)0x2e); put('/',      (byte)0x2f);
        put('0',      (byte)0x30); put('1',      (byte)0x31); put('2',      (byte)0x32); put('3',      (byte)0x33);
        put('4',      (byte)0x34); put('5',      (byte)0x35); put('6',      (byte)0x36); put('7',      (byte)0x37);
        put('8',      (byte)0x38); put('9',      (byte)0x39); put(':',      (byte)0x3a); put(';',      (byte)0x3b);
        put('<',      (byte)0x3c); put('=',      (byte)0x3d); put('>',      (byte)0x3e); put('?',      (byte)0x3f);
        put('¡',      (byte)0x40); put('A',      (byte)0x41); put('B',      (byte)0x42); put('C',      (byte)0x43);
        put('D',      (byte)0x44); put('E',      (byte)0x45); put('F',      (byte)0x46); put('G',      (byte)0x47);
        put('H',      (byte)0x48); put('I',      (byte)0x49); put('J',      (byte)0x4a); put('K',      (byte)0x4b);
        put('L',      (byte)0x4c); put('M',      (byte)0x4d); put('N',      (byte)0x4e); put('O',      (byte)0x4f);
        put('P',      (byte)0x50); put('Q',      (byte)0x51); put('R',      (byte)0x52); put('S',      (byte)0x53);
        put('T',      (byte)0x54); put('U',      (byte)0x55); put('V',      (byte)0x56); put('W',      (byte)0x57);
        put('X',      (byte)0x58); put('Y',      (byte)0x59); put('Z',      (byte)0x5a); put('Ä',      (byte)0x5b);
        put('Ö',      (byte)0x5c); put('Ñ',      (byte)0x5d); put('Ü',      (byte)0x5e); put('§',      (byte)0x5f);
        put('¿',      (byte)0x60); put('a',      (byte)0x61); put('b',      (byte)0x62); put('c',      (byte)0x63);
        put('d',      (byte)0x64); put('e',      (byte)0x65); put('f',      (byte)0x66); put('g',      (byte)0x67);
        put('h',      (byte)0x68); put('i',      (byte)0x69); put('j',      (byte)0x6a); put('k',      (byte)0x6b);
        put('l',      (byte)0x6c); put('m',      (byte)0x6d); put('n',      (byte)0x6e); put('o',      (byte)0x6f);
        put('p',      (byte)0x70); put('q',      (byte)0x71); put('r',      (byte)0x72); put('s',      (byte)0x73);
        put('t',      (byte)0x74); put('u',      (byte)0x75); put('v',      (byte)0x76); put('w',      (byte)0x77);
        put('x',      (byte)0x78); put('y',      (byte)0x79); put('z',      (byte)0x7a); put('ä',      (byte)0x7b);
        put('ö',      (byte)0x7c); put('ñ',      (byte)0x7d); put('ü',      (byte)0x7e); put('à',      (byte)0x7f);
    }};

    /**
     * Encodes a phone number the way the standard requires: two digits per
     * byte, with the digits of each pair swapped. When the amount of digits
     * is odd, the unused half byte is filled with 1111.
     *
     * @param number the number to encode
     * @param length how many digits to take from it
     */
    public static byte[] encodeNumber(long number, int length){
        Deque<Byte> cyfers = new ArrayDeque<>();
        for (int i = 0; i < length; i++) {
            byte n = (byte)(number % 10);
            number = number / 10;
            cyfers.push(n);
        }

        int size = (cyfers.size() / 2) + (cyfers.size() % 2);

        var res = new byte[size];

        for (int i = 0; i < size; i ++) {
            long n1 = cyfers.pop();
            long n2;
            if (cyfers.size() > 0) {
                n2 = cyfers.pop();
            }
            else {
                n2 = 0b1111;
            }
            n2 = n2 << 4;
            res[i] = (byte)(n1 | n2);
        }

        return res;
    }

    /** Reads back a number written by {@link #encodeNumber}. */
    public static long decodeNumber(byte[] bytes) {
        long res = 0;
        for (int i = 0; i < bytes.length; i++) {
            int number = bytes[i] & 0xFF;
            res = res * 10;
            res = res + (number & 0b1111);
            number = number >> 4;
            if ((number & 0b1111) != 0b1111) {
                res = res * 10;
                res = res + (number & 0b1111);
            }
        }
        return res;
    }

    /**
     * Encodes the service centre timestamp: year, month, day, hour, minute,
     * second and time zone, each as a pair of swapped digits. The zone is
     * counted in quarters of an hour, and bit 3 of its byte marks a negative
     * offset.
     */
    public static byte[] encodeDateTime(ZonedDateTime dateTime){
        int offsetInHourQuarters = dateTime.getOffset().getTotalSeconds() / 60 / 15;
        byte[] encodedOffset = encodeNumber(Math.abs(offsetInHourQuarters), 2);
        if(offsetInHourQuarters < 0){
            encodedOffset[0] = (byte)((int)encodedOffset[0] | 0b00000100);
        }
        return ByteBuffer.allocate(7)
            .put(encodeNumber(dateTime.getYear() % 100, 2)[0])
            .put(encodeNumber(dateTime.getMonthValue(), 2)[0])
            .put(encodeNumber(dateTime.getDayOfMonth(), 2)[0])
            .put(encodeNumber(dateTime.getHour(), 2)[0])
            .put(encodeNumber(dateTime.getMinute(), 2)[0])
            .put(encodeNumber(dateTime.getSecond(), 2)[0])
            .put(encodedOffset[0])
            .array();
    }
    
    /**
     * Packs the text with the GSM 7-bit alphabet.
     *
     * Every character takes seven bits instead of eight, so the codes do not
     * line up with byte borders: each one is written into the free bits of the
     * current byte, and the rest of it continues in the next byte. This is why
     * eight characters fit into seven bytes. Characters outside the alphabet
     * are skipped.
     */
    public static byte[] encodeMessage(String message) {
        var buffer = new byte[(message.length() * 7 / 8) + 1];
        var idx = 0;
        var available = 8;
        for(var c : message.toCharArray()) {
            if (charactersMap.containsKey(c)) {
                var firstPart = Math.min(7, available);
                var secondPart = 7 - firstPart;

                var code = charactersMap.get(c);
                int temp = code;
                temp = temp << 8 - available;
                buffer[idx] = (byte)(buffer[idx] | temp);
                available = available - firstPart;

                if (available == 0) {
                    idx++;
                    available = 8;
                }
                
                if (secondPart > 0) {
                    temp = code;
                    temp = temp >> 7 - secondPart;
                    buffer[idx] = (byte)(buffer[idx] | temp);
                    available = available - secondPart;
                }
            }
        }
        return buffer;
    }

    /**
     * Builds an address field: its length, one octet describing the number
     * type (international ISDN here) and the encoded number itself.
     */
    public static byte[] createSMSC(long number, int length) {

        int SMSC_TYPE = SMSC_TYPE_7_VAL | SMSC_TYPE_6_4_INTERNATIONAL | SMSC_TYPE_3_0_ISDN;

        byte[] SMSC_VALUE = encodeNumber(number, length);

        int SMSC_LENGTH = 1 + SMSC_VALUE.length;

        return ByteBuffer.allocate(2 + SMSC_VALUE.length)
            .put((byte)SMSC_LENGTH)
            .put((byte)SMSC_TYPE)
            .put(SMSC_VALUE)
            .array();
    }

    /**
     * Assembles a complete SMS-DELIVER message out of all its parts, in the
     * order the standard defines.
     */
    public static byte[] createSMS(String message, long fromNumber, long toNumber) {

        byte[] SMSC = createSMSC(fromNumber, 11);

        int FIRST_OCTET = TP_0_MTI_SMS_DELIVER 
            | TP_1_MMS_NO_MORE_MESSAGES
            | TP_5_SRI_VAL 
            | TP_6_UDHI_VAL
            | TP_7_RP_VAL;

        byte[] TP_OA = createSMSC(toNumber, 11);

        int TP_PID = TP_PID_7_6_VAL | TP_PID_5_VAL | TP_PID_4_0_IMPLICIT;

        int TP_DSC = TP_DSC_7_6_VAL | TP_DSC_5_UNCOMPRESSED | TP_DSC_4_NO_MESSAGE | TP_DSC_3_2_DEFAULT | TP_DSC_1_0_CLASS_0;

        byte[] TP_SCTS = encodeDateTime(ZonedDateTime.now());

        int TP_UDL = message.length();

        byte[] TP_UD = encodeMessage(message);

        return ByteBuffer.allocate(SMSC.length + 4 + TP_OA.length + TP_SCTS.length + TP_UD.length)
            .put(SMSC)
            .put((byte)FIRST_OCTET)
            .put(TP_OA)
            .put((byte)TP_PID)
            .put((byte)TP_DSC)
            .put(TP_SCTS)
            .put((byte)TP_UDL)
            .put(TP_UD)
            .array();
    }

    /**
     * Digs the recipient number out of a ready PDU. The fields have no fixed
     * offsets, so the length of each one is used to find the next.
     */
    public static long getToNumberFromSMS(byte[] bytes) {
        int SMSC_Idx = 0;
        int SMSC_Length = bytes[SMSC_Idx];
        int FIRST_OCTET_Idx = SMSC_Idx + SMSC_Length + 1;
        int TP_OA_Idx = FIRST_OCTET_Idx + 1;
        int TP_OA_Length = bytes[TP_OA_Idx] - 1;

        byte[] encodedNumber = new byte[TP_OA_Length];
        for (int i = 0; i < TP_OA_Length; i++) {
            encodedNumber[i] = bytes[TP_OA_Idx + 2 + i];
        }

        return PDU.decodeNumber(encodedNumber);
    }
}
