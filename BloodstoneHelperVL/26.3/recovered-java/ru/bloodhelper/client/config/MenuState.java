/*
 * Decompiled with CFR 0.152.
 */
package ru.bloodhelper.client.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public final class MenuState {
    public String page = "VISUALS";
    public String query = "";
    public List<String> expanded = new ArrayList<String>();
    public List<String> advanced = new ArrayList<String>();
    public double scroll;
    public double profileScroll;

    public void sanitize() {
        if (this.page == null || !Set.of("VISUALS", "STAFF", "APPEARANCE", "PROFILES", "ABOUT").contains(this.page)) {
            this.page = "VISUALS";
        }
        if (this.query == null) {
            this.query = "";
        }
        this.query = this.query.replaceAll("[\\p{Cntrl}]", "");
        if (this.query.length() > 80) {
            this.query = this.query.substring(0, 80);
        }
        this.expanded = MenuState.clean(this.expanded);
        this.advanced = MenuState.clean(this.advanced);
        this.scroll = MenuState.finiteScroll(this.scroll);
        this.profileScroll = MenuState.finiteScroll(this.profileScroll);
    }

    public MenuState copy() {
        MenuState result = new MenuState();
        result.page = this.page;
        result.query = this.query;
        result.scroll = this.scroll;
        result.profileScroll = this.profileScroll;
        result.expanded = this.expanded == null ? null : new ArrayList<String>(this.expanded);
        result.advanced = this.advanced == null ? null : new ArrayList<String>(this.advanced);
        result.sanitize();
        return result;
    }

    private static List<String> clean(List<String> ids) {
        return ids == null ? new ArrayList<String>() : new ArrayList<String>(ids.stream().filter(Objects::nonNull).filter(id -> id.matches("[A-Za-z0-9_.:-]{1,100}")).distinct().limit(128L).toList());
    }

    private static double finiteScroll(double value) {
        return Double.isFinite(value) ? Math.clamp(value, 0.0, 100000.0) : 0.0;
    }
}

