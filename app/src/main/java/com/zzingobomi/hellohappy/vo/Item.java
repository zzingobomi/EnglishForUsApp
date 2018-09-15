package com.zzingobomi.hellohappy.vo;

import java.io.Serializable;
import java.util.Date;

public class Item implements Serializable{
    private int idx;
    private String title_ko;
    private String title_en;
    private String addinfo;
    private Date regdate;
    private String regidemail;
    private String regdisplayname;
    private int replycnt;
    private int likecnt;
    private int badcnt;
    private int impressioncnt;

    // 요청 유저의 상태값
    private boolean likestate = false;
    private boolean badstate = false;

    public Item() {

    }

    public Item(int idx, String title_ko, String title_en, String addinfo) {
        this.title_ko = title_ko;
        this.title_en = title_en;
        this.addinfo = addinfo;
    }

    public Item(String title_ko, String title_en, String addinfo, String regidemail, String regdisplayname) {
        this.title_ko = title_ko;
        this.title_en = title_en;
        this.addinfo = addinfo;
        this.regidemail = regidemail;
        this.regdisplayname = regdisplayname;
    }

    public Item(String title_ko, String title_en, String addinfo, String regidemail, String regdisplayname, int replycnt, int likecnt, int badcnt, int impressioncnt) {
        this.title_ko = title_ko;
        this.title_en = title_en;
        this.addinfo = addinfo;
        this.regidemail = regidemail;
        this.regdisplayname = regdisplayname;
        this.replycnt = replycnt;
        this.likecnt = likecnt;
        this.badcnt = badcnt;
        this.impressioncnt = impressioncnt;
    }

    public int getIdx() {
        return idx;
    }

    public void setIdx(int idx) {
        this.idx = idx;
    }

    public String getTitle_ko() {
        return title_ko;
    }

    public void setTitle_ko(String title_ko) {
        this.title_ko = title_ko;
    }

    public String getTitle_en() {
        return title_en;
    }

    public void setTitle_en(String title_en) {
        this.title_en = title_en;
    }

    public String getAddinfo() {
        return addinfo;
    }

    public void setAddinfo(String addinfo) {
        this.addinfo = addinfo;
    }

    public String getRegidemail() {
        return regidemail;
    }

    public Date getRegdate() {
        return regdate;
    }

    public void setRegdate(Date regdate) {
        this.regdate = regdate;
    }

    public void setRegidemail(String regidemail) {
        this.regidemail = regidemail;
    }

    public String getRegdisplayname() {
        return regdisplayname;
    }

    public void setRegdisplayname(String regdisplayname) {
        this.regdisplayname = regdisplayname;
    }

    public int getReplycnt() {
        return replycnt;
    }

    public void setReplycnt(int replycnt) {
        this.replycnt = replycnt;
    }

    public int getLikecnt() {
        return likecnt;
    }

    public void setLikecnt(int likecnt) {
        this.likecnt = likecnt;
    }

    public int getBadcnt() {
        return badcnt;
    }

    public void setBadcnt(int badcnt) {
        this.badcnt = badcnt;
    }

    public int getImpressioncnt() {
        return impressioncnt;
    }

    public void setImpressioncnt(int impressioncnt) {
        this.impressioncnt = impressioncnt;
    }

    public boolean isLikestate() {
        return likestate;
    }

    public void setLikestate(boolean likestate) {
        this.likestate = likestate;
    }

    public boolean isBadstate() {
        return badstate;
    }

    public void setBadstate(boolean badstate) {
        this.badstate = badstate;
    }

    @Override
    public String toString() {
        return "Item{" +
                "idx=" + idx +
                ", title_ko='" + title_ko + '\'' +
                ", title_en='" + title_en + '\'' +
                ", addinfo='" + addinfo + '\'' +
                ", regdate=" + regdate +
                ", regidemail='" + regidemail + '\'' +
                ", regdisplayname='" + regdisplayname + '\'' +
                ", replycnt=" + replycnt +
                ", likecnt=" + likecnt +
                ", badcnt=" + badcnt +
                ", impressioncnt=" + impressioncnt +
                ", likestate=" + likestate +
                ", badstate=" + badstate +
                '}';
    }
}
