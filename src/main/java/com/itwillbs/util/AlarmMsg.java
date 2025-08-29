package com.itwillbs.util;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.Locale;

public class AlarmMsg {

    private static String n(int x){ return NumberFormat.getNumberInstance(Locale.KOREA).format(x); }

    public static String materialShortage(String name, int available, int safety, String unit){
        return String.format("⚠️ [자재부족] %s 재고 %s%s (안전 %s%s) — 발주 검토 부탁드려요",
                name, n(available), unit, n(safety), unit);
    }

    public static String finishedShortage(String name, int available, int threshold, String unit){
        return String.format("⚠️ [완제품부족] %s 가용 %s%s (안전 %s%s) — 생산/출고 계획 점검 부탁드려요",
                name, n(available), unit, n(threshold), unit);
    }

    public static String newOrder(String client, String product, int qty, String unit, LocalDate due){
        return String.format("📦 [신규수주] %s / %s %s%s — 납기 %s 확인 부탁드려요",
                client, product, n(qty), unit, due);
    }

    public static String workOrderNeeded(String orderId, String product, int qty, String unit, LocalDate due){
        return String.format("🛠️ [작업지시 요청] %s %s%s — 수주 %s, 납기 %s",
                product, n(qty), unit, orderId, due);
    }

    public static String notice(String text){
        return "🔔 " + text + " — 확인 부탁드려요";
    }
}