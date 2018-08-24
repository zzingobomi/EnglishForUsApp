package com.zzingobomi.englishforus.vo;

public class RankingManyItemVO {
    private String regdisplayname;
    private String regphotourl;
    private Integer count;

    public RankingManyItemVO() {

    }

    public RankingManyItemVO(String regdisplayname, String regphotourl, Integer count) {
        this.regdisplayname = regdisplayname;
        this.regphotourl = regphotourl;
        this.count = count;
    }

    public String getRegdisplayname() {
        return regdisplayname;
    }

    public void setRegdisplayname(String regdisplayname) {
        this.regdisplayname = regdisplayname;
    }

    public String getRegphotourl() {
        return regphotourl;
    }

    public void setRegphotourl(String regphotourl) {
        this.regphotourl = regphotourl;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    @Override
    public String toString() {
        return "RankingManyItemVO{" +
                "regdisplayname='" + regdisplayname + '\'' +
                ", regphotourl='" + regphotourl + '\'' +
                ", count=" + count +
                '}';
    }
}
