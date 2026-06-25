package org.devore.lang.token;

import org.devore.exception.DevoreRuntimeException;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 二进制数据
 */
public class DBinary extends DToken {
    private byte[] bytes;    // 字节序列

    private DBinary(byte[] bytes) {
        this.bytes = Arrays.copyOf(bytes, bytes.length);
    }

    public static DBinary valueOf(byte[] bytes) {
        return new DBinary(bytes);
    }

    /**
     * 清空二进制数据
     */
    public void clear() {
        this.bytes = new byte[0];
    }

    /**
     * 获取字节
     *
     * @param index 位置
     * @return 字节
     */
    public int get(int index) {
        checkIndex(index, "访问");
        return Byte.toUnsignedInt(this.bytes[index]);
    }

    /**
     * 设置字节
     *
     * @param index 位置
     * @param value 字节值
     * @param force 是否更改原数据
     * @return 结果
     */
    public DBinary set(int index, int value, boolean force) {
        checkIndex(index, "写入");
        checkByte(value);
        byte[] result = force ? this.bytes : Arrays.copyOf(this.bytes, this.bytes.length);
        result[index] = (byte) value;
        if (force)
            return this;
        return DBinary.valueOf(result);
    }

    /**
     * 添加字节
     *
     * @param value 字节值
     * @param force 是否更改原数据
     * @return 结果
     */
    public DBinary add(int value, boolean force) {
        checkByte(value);
        byte[] result = Arrays.copyOf(this.bytes, this.bytes.length + 1);
        result[result.length - 1] = (byte) value;
        if (force) {
            this.bytes = result;
            return this;
        }
        return DBinary.valueOf(result);
    }

    /**
     * 添加字节
     *
     * @param index 位置
     * @param value 字节值
     * @param force 是否更改原数据
     * @return 结果
     */
    public DBinary add(int index, int value, boolean force) {
        checkAddIndex(index);
        checkByte(value);
        byte[] result = new byte[this.bytes.length + 1];
        System.arraycopy(this.bytes, 0, result, 0, index);
        result[index] = (byte) value;
        System.arraycopy(this.bytes, index, result, index + 1, this.bytes.length - index);
        if (force) {
            this.bytes = result;
            return this;
        }
        return DBinary.valueOf(result);
    }

    /**
     * 拼接二进制数据
     *
     * @param other 另一个二进制数据
     * @param force 是否更改原数据
     * @return 结果
     */
    public DBinary concat(DBinary other, boolean force) {
        byte[] result = Arrays.copyOf(this.bytes, this.bytes.length + other.bytes.length);
        System.arraycopy(other.bytes, 0, result, this.bytes.length, other.bytes.length);
        if (force) {
            this.bytes = result;
            return this;
        }
        return DBinary.valueOf(result);
    }

    /**
     * 截取
     *
     * @param fromIndex 起始位置
     * @param toIndex   结束位置
     * @param force     是否更改原数据
     * @return 截取后的二进制数据
     */
    public DBinary subBinary(int fromIndex, int toIndex, boolean force) {
        if (fromIndex > toIndex)
            throw new DevoreRuntimeException("二进制截取起始下标大于目标下标, fromIndex=" + fromIndex
                    + ", toIndex=" + toIndex + ", length=" + this.bytes.length + ".");
        if (fromIndex < 0 || fromIndex > this.bytes.length)
            throw new DevoreRuntimeException("二进制截取过界, fromIndex=" + fromIndex + ", 但二进制只有"
                    + this.bytes.length + "个字节.");
        if (toIndex > this.bytes.length)
            throw new DevoreRuntimeException("二进制截取过界, toIndex=" + toIndex + ", 但二进制只有"
                    + this.bytes.length + "个字节.");
        byte[] result = Arrays.copyOfRange(this.bytes, fromIndex, toIndex);
        if (force) {
            this.bytes = result;
            return this;
        }
        return DBinary.valueOf(result);
    }

    /**
     * 获取字节数量
     *
     * @return 字节数量
     */
    public int size() {
        return this.bytes.length;
    }

    /**
     * 转换为Java的byte数组
     *
     * @return 字节数组
     */
    public byte[] toByteArray() {
        return Arrays.copyOf(this.bytes, this.bytes.length);
    }

    /**
     * 转换为Devore列表
     *
     * @return 字节值列表
     */
    public List<DToken> toList() {
        return IntStream.range(0, this.bytes.length)
                .mapToObj(i -> DNumber.valueOf(Byte.toUnsignedInt(this.bytes[i])))
                .collect(Collectors.toList());
    }

    /**
     * 转成十六进制字符串
     *
     * @return 十六进制字符串
     */
    public String toHex() {
        return IntStream.range(0, this.bytes.length)
                .mapToObj(i -> String.format("%02x", Byte.toUnsignedInt(this.bytes[i])))
                .collect(Collectors.joining());
    }

    @Override
    public String type() {
        return "binary";
    }

    @Override
    protected String str() {
        return "#binary(" + this.toHex() + ")";
    }

    @Override
    public int compareTo(DToken t) {
        if (!(t instanceof DBinary))
            return -1;
        return Arrays.equals(this.bytes, ((DBinary) t).bytes) ? 0 : -1;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.type(), Arrays.hashCode(this.bytes));
    }

    private void checkIndex(int index, String action) {
        if (index < 0 || index >= this.bytes.length)
            throw new DevoreRuntimeException("二进制" + action + "过界, 下标=" + index
                    + ", 但二进制只有" + this.bytes.length + "个字节.");
    }

    private void checkAddIndex(int index) {
        if (index < 0 || index > this.bytes.length)
            throw new DevoreRuntimeException("二进制添加过界, 下标=" + index
                    + ", 但二进制只有" + this.bytes.length + "个字节.");
    }

    private static void checkByte(int value) {
        if (value < 0 || value > 255)
            throw new DevoreRuntimeException("字节值必须在0到255之间: " + value + ".");
    }

    public static DBinary fromHex(String hex) {
        String value = hex.trim().toLowerCase(Locale.ROOT);
        if (value.length() % 2 != 0)
            throw new DevoreRuntimeException("十六进制字符串长度必须为偶数.");
        byte[] result = new byte[value.length() / 2];
        for (int i = 0; i < value.length(); i += 2) {
            int high = Character.digit(value.charAt(i), 16);
            int low = Character.digit(value.charAt(i + 1), 16);
            if (high < 0 || low < 0)
                throw new DevoreRuntimeException("十六进制字符串包含非法字符: " + hex + ".");
            result[i / 2] = (byte) ((high << 4) | low);
        }
        return DBinary.valueOf(result);
    }
}
