package com.enjin.velocity.utils.io;

import java.io.IOException;
import java.io.RandomAccessFile;

public class ReverseFileReader {
    private final RandomAccessFile randomAccessFile;
    private long position;

    public ReverseFileReader(String fileName) throws Exception {
        this.randomAccessFile = new RandomAccessFile(fileName, "r");
        this.position = this.randomAccessFile.length();
        this.randomAccessFile.seek(this.position);

        String currentLine = this.randomAccessFile.readLine();
        while (currentLine == null) {
            if (this.position < 0) {
                break;
            }

            this.position--;
            this.randomAccessFile.seek(this.position);
            currentLine = this.randomAccessFile.readLine();
            this.randomAccessFile.seek(this.position);
        }
    }

    public String readLine() throws Exception {
        int thisCode;
        char thisChar;
        StringBuilder finalLine = new StringBuilder();

        if (this.position < 0) {
            return null;
        }

        while (true) {
            if (this.position < 0) {
                break;
            }

            this.randomAccessFile.seek(this.position);

            thisCode = this.randomAccessFile.readByte();
            thisChar = (char) thisCode;

            if (thisChar == 13 || thisCode == 10) {
                this.randomAccessFile.seek(this.position - 1);
                int nextCode = this.randomAccessFile.readByte();
                if((thisCode == 10 && nextCode == 13) || (thisCode == 13 && nextCode == 10)) {
                    this.position = this.position -1;
                }
                this.position--;
                break;
            } else {
                finalLine.insert(0, thisChar);
            }
            this.position--;
        }
        return finalLine.toString();
    }

    public void close() {
        try{
            randomAccessFile.close();;
        } catch (IOException ignored) {
        }
    }
}
