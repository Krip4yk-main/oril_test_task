package com.Krip4yk.oril_test_task;

import org.json.JSONArray;
import org.json.JSONObject;

import javax.xml.xpath.XPath;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        String requestURL = "https://cex.io/api/last_price/";
        String btc="BTC/USD", eth="ETH/USD", xrp="XRP/USD";
        double BTC=0, ETH=0, XRP=0;
        try {
            BTC = Double.parseDouble(new JSONReader().readJsonFromUrl(requestURL+btc).get("lprice").toString());
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        try {
            ETH = Double.parseDouble(new JSONReader().readJsonFromUrl(requestURL+eth).get("lprice").toString());
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        try {
            XRP = Double.parseDouble(new JSONReader().readJsonFromUrl(requestURL+xrp).get("lprice").toString());
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
    }
}
