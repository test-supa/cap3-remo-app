package com.hmdm.control;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class Utils {
    public static String generateTransactionId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public static String byteBufferToString(ByteBuffer buffer) {
        if (buffer == null) return null;
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public static ByteBuffer stringToByteBuffer(String s) {
        if (s == null) return null;
        return ByteBuffer.wrap(s.getBytes(StandardCharsets.UTF_8));
    }
}
