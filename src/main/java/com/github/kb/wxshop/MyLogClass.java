package com.github.kb.wxshop;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyLogClass {
    private static final Logger logger = LoggerFactory.getLogger(MyLogClass.class);

    public static void main(String[] args) {
        logger.debug("debug");
        logger.info("info");
    }
}
