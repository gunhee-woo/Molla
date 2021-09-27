package com.dlog.molla;

/**
 * 오픈소르 라이브러리에 대한 정보를 담는 데이터 클래스
 *
 * @author DLOG
 * @version 1.0.0 20/04/13
 */

public class OpenSourceInfo {
    /**
     * 오픈소스 라이브러리 개발자 이름
     */
    private String mName;
    /**
     * 오픈소스 라이브러리 저작권
     */
    private String mCopyright;
    /**
     * 오픈소스 라이브러리 깃허브 주소
     */
    private String mWebAddress;
    /**
     * 오픈소스 라이브러리 라이센스
     */
    private String mLicense;

    /**
     * OpenSourceInfo 생성자
     * @param mName 오픈소스 라이브러리 개발자 이름
     * @param mCopyright 오픈소스 라이브러리 저작권
     * @param mLicense 오픈소스 라이브러리 라이센스
     * @param mWebAddress 오픈소스 라이브러리 깃허브 주소
     */

    public OpenSourceInfo(String mName, String mCopyright, String mLicense, String mWebAddress){
        this.mCopyright = mCopyright;
        this.mLicense = mLicense;
        this.mName = mName;
        this.mWebAddress = mWebAddress;
    }

    /**
     * 오픈소스 라이브러리 저작권 내용을 반환
     * @return 오픈소스 라이브러리 저작권
     */

    public String getmCopyright() {
        return mCopyright;
    }

    /**
     * 오픈소스 라이브러리 라이센스 내용을 반환
     * @return 오픈소스 라이브러리 라이센스
     */

    public String getmLicense() {
        return mLicense;
    }

    /**
     * 오픈소스 라이브러리 개발자 이름을 반환
     * @return 오픈소스 라이브러리 개발자 이름
     */

    public String getmName() {
        return mName;
    }

    /**
     * 오픈소스 라이브러리 깃허브 주소 내용을 반환
     * @return 오픈소스 라이브러리 깃허브 주소
     */

    public String getmWebAddress() {
        return mWebAddress;
    }
}
