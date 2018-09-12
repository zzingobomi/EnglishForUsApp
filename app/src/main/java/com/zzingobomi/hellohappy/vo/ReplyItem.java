package com.zzingobomi.hellohappy.vo;

import java.util.Date;

public class ReplyItem {
    private int idx;
    private int itemidx;
    private String replytext;
    private String regidemail;
    private String regdisplayname;
    private Date regdate;
    private Date updatedate;
    private String regphotourl;
    private Integer likecnt;
    private Integer badcnt;

    // 요청 유저의 상태값
    private boolean likestate = false;
    private boolean badstate = false;

    public int getIdx() {
        return idx;
    }

    public void setIdx(int idx) {
        this.idx = idx;
    }

    public int getItemidx() {
        return itemidx;
    }

    public void setItemidx(int itemidx) {
        this.itemidx = itemidx;
    }

    public String getReplytext() {
        return replytext;
    }

    public void setReplytext(String replytext) {
        this.replytext = replytext;
    }

    public String getRegidemail() {
        return regidemail;
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

    public Date getRegdate() {
        return regdate;
    }

    public void setRegdate(Date regdate) {
        this.regdate = regdate;
    }

    public Date getUpdatedate() {
        return updatedate;
    }

    public void setUpdatedate(Date updatedate) {
        this.updatedate = updatedate;
    }

    public String getRegphotourl() {
        return regphotourl;
    }

    public void setRegphotourl(String regphotourl) {
        this.regphotourl = regphotourl;
    }

    public Integer getLikecnt() {
        return likecnt;
    }
    public void setLikecnt(Integer likecnt) {
        this.likecnt = likecnt;
    }
    public Integer getBadcnt() {
        return badcnt;
    }
    public void setBadcnt(Integer badcnt) {
        this.badcnt = badcnt;
    }

    // 요청 유저의 상태값
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
        return "ReplyItem{" +
                "idx=" + idx +
                ", itemidx=" + itemidx +
                ", replytext='" + replytext + '\'' +
                ", regidemail='" + regidemail + '\'' +
                ", regdisplayname='" + regdisplayname + '\'' +
                ", regdate=" + regdate +
                ", updatedate=" + updatedate +
                ", regphotourl='" + regphotourl + '\'' +
                ", likecnt=" + likecnt +
                ", badcnt=" + badcnt +
                ", likestate=" + likestate +
                ", badstate=" + badstate +
                '}';
    }
}
