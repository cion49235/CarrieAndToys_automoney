package carrie.toy.friends.automoney.util;

import android.text.InputFilter;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.util.Base64;
import android.util.Log;
import android.widget.TextView;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class StringUtil {

	private static final String regex = "^[_A-Za-z0-9-]+(.[_A-Za-z0-9-]+)*@(?:\\w+\\.)+\\w+$";
	private final static String C_BASE_KEY_STRING = "SMARTMOUMOU2015004";

	/**
	 * MD5 데이터 암호화
	 * @param str
	 * @return
	 */
	public static String toEncMD5(String str)
	{
		String MD5 = "";
		try{
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(str.getBytes()); 
			byte byteData[] = md.digest();
			StringBuffer sb = new StringBuffer();
			for(int i = 0 ; i < byteData.length ; i++){
				sb.append(Integer.toString((byteData[i]&0xff) + 0x100, 16).substring(1));
			}
			MD5 = sb.toString();
			
		}catch(NoSuchAlgorithmException e){
			e.printStackTrace(); 
			MD5 = null; 
		}
		return MD5;
	}
	
	/**
	 * AES 데이터 복호화
	 * @param encrypted
	 * @return
	 * @throws Exception
	 */
	public static String decrypt(String encrypted)
	{
	    SecretKeySpec skeySpec = new SecretKeySpec( C_BASE_KEY_STRING.getBytes(), "AES");
		Cipher cipher;
		String decryptedString = "";
		byte[] decrypted;
		try {
			cipher = Cipher.getInstance("AES/ECB/PKCS7Padding", "BC");
			cipher.init(Cipher.DECRYPT_MODE, skeySpec);
			byte[] decordedValue = Base64.decode(encrypted.getBytes(), Base64.NO_WRAP);
		    decrypted = cipher.doFinal(decordedValue);
		    decryptedString = new String(decrypted,"UTF-8");
		} catch (Exception e) {
			Log.d("Exception e", e.getMessage());
		}
		return decryptedString;
	}
	
	/**
     * AES 방식의 암호화
     * 
     * @param message
     * @return
     * @throws Exception
     */
    public static String encrypt(String message) throws Exception {

        SecretKeySpec skeySpec = new SecretKeySpec( C_BASE_KEY_STRING.getBytes(), "AES");

        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec);

        if(message == null)
    		return null;
        
        byte[] dataStringBytes = message.getBytes("UTF-8");
        byte[] encrypted = cipher.doFinal(dataStringBytes);
        return Base64.encodeToString(encrypted, Base64.NO_WRAP);
    }
	
    /**
	 * 이메일 유효성 체크
	 * @param check
	 * @return
	 */
	/*public static boolean isValidEmail(String check) {
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(check);
		return m.matches();
    }*/

	/**
	 * 이름이 유효한 형식 체크 (한글, 영문, 숫자만 허용)
	 * @param s
	 * @return
	 */
	public static boolean isNameValid(String s)
	{
		String strKeywordU      = s.toUpperCase();
		Pattern p = Pattern.compile(".*[^가-힣a-zA-Z0-9 ].*");
		Matcher m = p.matcher(strKeywordU);
		if(m.matches()) {
		   return false;
		} else {
		   return true;
		}
	}
	
	/**
	 * 아이디가 유효한 형식 체크 (영문, 숫자만 허용)
	 * @param s
	 * @return
	 */
	public static boolean isIDValid(String s)
	{
		String strKeywordU      = s.toUpperCase();
		Pattern p = Pattern.compile("^[a-zA-Z]{1}[a-zA-Z0-9_]{4,20}$");
		Matcher m = p.matcher(strKeywordU);
		if(m.matches()) {
		   return true;
		} else {
		   return false;
		}
	}
	/**
	 * 비밀번호가 유효한 형식 체크 (영문, 숫자만 허용)
	 * @param s
	 * @return
	 */
	public static boolean isPasswordValid(String s)
	{
		String strKeywordU      = s.toUpperCase();
		Pattern p = Pattern.compile("^[a-zA-Z0-9_]{6,20}$");
		Matcher m = p.matcher(strKeywordU);
		if(m.matches()) {
		   return true;
		} else {
		   return false;
		}
	}

	/**
	 * 휴대폰번호 형식 체크
	 * @param cellphoneNumber
	 * @return
	 */
	public static boolean isValidCellPhoneNumber(String cellphoneNumber) {
		boolean returnValue = false;
		String regex = "^\\s*(010|011|012|013|014|015|016|017|018|019)(-|\\)|\\s)*(\\d{3,4})(-|\\s)*(\\d{4})\\s*$";
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(cellphoneNumber);
		if (m.matches()) {
			returnValue = true;
		}
		return returnValue;
	}

	/**
	 * 주소정보
	 * @param addr
	 * @return
	 */
	public static String getShortAddr(String addr)
	{
		if( addr == null)
			return "";
		
		String[] values = addr.split(" ");
		
		if( values.length == 0 ) {
			return addr;
		}
		else if( values.length == 1 ) {
			return values[0];
		}
		else if( values.length == 2 ) {
			return values[0] + " " + values[1];
		}
		else {
			return values[0] + " " + values[1] + " " + values[2];
		}
	}
	/**
	 * 문자열이 정수숫자로만 이루어졌는지 비교
	 * @param value
	 * @return
	 */
	public static boolean isNumber(String value)
	{
		if( value == null || value.length()  == 0 )
			return false;
		
		for(char c : value.toCharArray() ) {
			if( c < '0' || c > '9')
				return false;
		}
		
		return true;
	}
	
	/**
	 * 대상 Object가 널이면 기본 빈물자열이 리턴이 되며 널이 아닐경우 입력 파라미터를 ToString() 처리 한다.
	 * @param object : ToString 대상 Object
	 * @return parameter objecct.ToString() 
	 */
	public static String nvl(Object object) {
		return object != null ? object.toString() : "";
	}
		
	/**
	 * 문자열이 null 일때 임의의 문자열을 반환한다.
	 * @param  String value, String defaultValue
	 * @return String
	 * @throws
	 */
	public static String nvl(String value, String defaultValue) {
	    return (value == null || "".equals(value)) ? defaultValue : value.trim();
	}

	public static String nvl(Object o, String defaultValue) {
	    return (o == null) ? defaultValue : o.toString().trim();
	}
	
	public static String substrMax(String str, int maxlength){
		if( str == null) return "";
		return str.length() >  maxlength ? str.substring(0, maxlength) : str;
	}
	
	/**
	 * <p>
	 * 입력 파라미터의 문자열의 존재여부 체크
	 * </p>
	 * @param str the String to check, may be null
	 * @return <code>true</code> if the String is empty or null
	 */
	public static boolean isEmpty(String str) {
		return str == null || str.trim().length() == 0 || "NULL".equals(str) || "null".equals(str);
	}
	
	/**
	 * 문자열을 Replace 한다.
	 * @param text
	 * @param repl
	 * @param with
	 * @param max
	 * @return
	 */
	public static String replace(String text, String repl, String with, int max) {
		if (isEmpty(text) || isEmpty(repl) || with == null || max == 0) {
			return text;
		}
		int start = 0;
		int end = text.indexOf(repl, start);
		if (end == -1) {
			return text;
		}
		int replLength = repl.length();
		int increase = with.length() - replLength;
		increase = (increase < 0 ? 0 : increase);
		increase *= (max < 0 ? 16 : (max > 64 ? 64 : max));
		StringBuffer buf = new StringBuffer(text.length() + increase);
		while (end != -1) {
			buf.append(text.substring(start, end)).append(with);
			start = end + replLength;
			if (--max == 0) {
				break;
			}
			end = text.indexOf(repl, start);
		}
		buf.append(text.substring(start));
		return buf.toString();
	}
	
	/**
	 * Object => int 형 변환
	 * @param value
	 * @return
	 */
	public static int toInt(Object value) {
		int ret = -1;
		
		if(value == null || "".equals(value)) return ret;
		
		try {
			if (value instanceof java.math.BigInteger) {
				return ((java.math.BigInteger)value).intValue();
			} else if (value instanceof String) {
				return Integer.parseInt((String)value);
			} else {
				return Integer.parseInt(value.toString());
			}
		} catch(Exception ex) {
			ex.printStackTrace();
		}
		
		return ret;
	}
	
	/**
	 * 2015-02-27T09:04:51 > 2015.02.27
	 * 날짜만 가져오기 
	 */
	public static String getDateStr(String date, String replace) {
		String str = date.split("T")[0];
		str = str.replace("-", replace);
		return str;
	}
	
	/**
	 * 2015-02-27T09:04:51 > 2015.02.27 09:04:51
	 * 날짜만 가져오기 
	 */
	public static String getDateTimeStr(String date, String replace) {
		String str = "";
		String[] dateArr = date.split("T");
		str = dateArr[0].replace("-", replace) + " " + dateArr[1];
		return str;
	}
	
	/**
	 * toKSC5061 에서 UTF-8로 parsing
	 * @param s
	 * @return
	 */
	public static String toKSC5601(String s) {
		if (s == null) {
			return null;
		}
		try {
			return new String(s.getBytes("ISO-8859-1"), "UTF-8");
		} catch (Exception e) {
			return s;
		}
	}
	
	/**
	 * 학습시간 분으로 계산하기 (212 >> 03:32)
	 */
	public static String getSecondToTimeStr(int time) {
		
		StringBuffer time_str = new StringBuffer();
		
		int minute = time / 60;
		int second = time % 60;
		
		if(minute < 10) time_str.append("0" + minute);
		else time_str.append(minute);
		
		time_str.append(":");
		
		if(second < 10) time_str.append("0" + second);
		else time_str.append(second);
		
		return time_str.toString();
	}
	
	/**
	 * 학습시간 분으로 계산하기2 (212 >> 03분 32초)
	 */
	public static String getSecondToTimeStr2(int second) {
		
		int hh = (second / 3600);
		int mm = (second % 3600 / 60);
		int ss = (second % 3600 % 60);
		
		StringBuffer time = new StringBuffer();
		
		if(hh > 0) time.append(String.format("%02d", hh) + "시간 ");
		if(mm > 0) time.append(String.format("%02d", mm) + "분 ");
		time.append(String.format("%02d", ss) + "초");
		
		return time.toString();
	}
	
	
	/**
	* 0 ~ 9,A~F까지 범위 내에서 Random 하게 아스키값을 생성한다.
	*/
	public static String getGenSeqNo(int index) throws Exception {
		int count = 0;
		String tmp;
		
		byte[] randomByte = new byte[index];
		Random rr = new Random();
		for (int i = 0; i < index; i++) 
		{
			count = 48 + rr.nextInt(index);
			
			if((57 < count) && (count < 65))
				count += 7;
			randomByte[i] = (byte)count;
		}

		tmp = new String(randomByte);
		return tmp;
	}
	
	private static char[] _randomKeyLChar;
	
	/**
	 * 임의키 생성
	 */
	public static String getRandomKey(int len) throws Exception {
		
		StringBuffer buff = new StringBuffer();
		
		Random random = new Random();
		
		if( _randomKeyLChar == null ) {
			_randomKeyLChar = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890".toCharArray();
		}
		
		for ( int ii = 0; ii < len; ii++) {
			buff.append(_randomKeyLChar[random.nextInt(_randomKeyLChar.length)]);
		}
		
		return buff.toString();
	}
	
	/**
	 * 문자열 중복된값 제거
	 * @param str
	 * @return
	 */
	public static String getRepeatRemove(String str)
	{
		StringBuilder sb = new StringBuilder();
		String[] tokens = str.split(",");
		
		ArrayList<String> list = new ArrayList<String>();
		
		for( String msg : tokens)
		{
			list.add(msg);
		}

		ArrayList<String> nonDupList = new ArrayList<String>();
		
		Iterator<String> dupIter = list.iterator();
		
		while(dupIter.hasNext())
		{
			String msg = dupIter.next();
			
			if(nonDupList.contains(msg))
			{
				dupIter.remove();
			}else
			{
				nonDupList.add(msg);
			}
		}
		
		for(int i = 0; i < nonDupList.size(); i++)
		{
			String s = nonDupList.get(i);
			if( i == nonDupList.size() - 1)
			{
				sb.append(s);
			}else{
				sb.append(s + ",");
			}
		}
		return sb.toString();		
	}
	
	public static InputFilter filterAlphaNum = new InputFilter() {

		@Override
		public CharSequence filter(CharSequence source, int start, int end,
                                   Spanned dest, int dstart, int dend) {

			Pattern ps = Pattern.compile("^[a-zA-Z0-9]+$");
			if (!ps.matcher(source).matches()) {
				return "";
			} 
			return null;
		} 
	};
	
	public static InputFilter filterPassWord = new InputFilter() {

		@Override
		public CharSequence filter(CharSequence source, int start, int end,
                                   Spanned dest, int dstart, int dend) {

			Pattern ps = Pattern.compile("^[a-zA-Z0-9@!#*[$]]+$");
			if (!ps.matcher(source).matches()) {
				return "";
			} 
			return null;
		} 
	};
	
	public static InputFilter filterEmail = new InputFilter() {

		@Override
		public CharSequence filter(CharSequence source, int start, int end,
                                   Spanned dest, int dstart, int dend) {

			Pattern ps = Pattern.compile("^[@_a-zA-Z0-9-\\.]+$");
			if (!ps.matcher(source).matches()) {
				return "";
			} 
			return null;
		} 
	};
	
	public static String setDate(String paramString){
		String str1 = paramString;
		try{
	      paramString = paramString.replace("  ", " ");
	      String str2 = new SimpleDateFormat("yy.MM.dd.HH.mm.ss").format(new Date(Date.parse(paramString)));
	      str1 = str2;
	      return str1;
	    }catch (NullPointerException localNullPointerException){
	    	while (true)
	        str1 = "00.00.00 00.00.00";
	    }catch (IllegalArgumentException localIllegalArgumentException){
	    	while (true)
	        str1 = setDate2(paramString);
	    }
	}
	
	public static String setDate2(String paramString){
		String str1 = paramString;
	    try{
	    	String str2 = paramString.replace("KST", "+0900");
	    	String str3 = new SimpleDateFormat("yy.MM.dd.HH.mm.ss").format(new Date(Date.parse(str2)));
	    	str1 = str3;
	    	return str1;
	    }catch (NullPointerException localNullPointerException){
	    	while (true)
	        str1 = "00.00.00 00.00.00";
	    }catch (IllegalArgumentException localIllegalArgumentException){
	    	while (true)
	        str1 = "00.00.00 00.00.00";
	    }
	}
	
	public static String setDateTrim(String paramString){
		return paramString.substring(0, 8);
	}
	
	public static String getExtension(String fileStr) {
		return fileStr.substring(fileStr.lastIndexOf(".") + 1, fileStr.length());
	}
	
	public static int getRandomNumber(){
		return 1000 + (int)(9000.0D * Math.random());
	}
	
	public static String getUrlType(String s, String s1){
    	String as[] = s.split("url=");
    	String s2 = null;
    	int i = 0;
        do{
            if(i >= as.length)
                return s2;
            if(as[i].contains("medium") && as[i].contains(s1))
                s2 = removeItag2(removeComma(removeItag(removeCodecs(as[i]))));
            i++;
        } while(true);
    }
	
	public static String removeCodecs(String s){
        if(s.indexOf("codecs") > -1)
        {
            int i = s.indexOf(";");
            int j = s.indexOf("&", i);
            if(j == -1)
                j = -1 + s.length();
            String s1 = s.substring(0, i);
            String s2 = s.substring(j);
            if(s2.length() == 1)
                s = s1;
            else
                s = (new StringBuilder(String.valueOf(s1))).append(s2).toString();
        }
        return s;
    }
	
	public static String removeItag(String s)
    {
        if(getStringPatternCount(s, "&itag=") > 1)
        {
            int i = s.indexOf("&itag=");
            int j = s.indexOf("&", i + 1);
            String s1 = s.substring(0, i);
            String s2 = s.substring(j);
            s = (new StringBuilder(String.valueOf(s1))).append(s2).toString();
        }
        return s;
    }
	
	public static int getStringPatternCount(String s, String s1)
    {
        int i = 0;
        Matcher matcher = Pattern.compile(s1).matcher(s);
        int j = 0;
        do
        {
            if(!matcher.find(i))
                return j;
            j++;
            i = matcher.end();
        } while(true);
    }
	
	public static String removeComma(String s)
    {
        if(s != null && s.endsWith(","))
            s = s.substring(0, -1 + s.length());
        return s;
    }
	
	public static String removeItag2(String s)
    {
        if(getStringPatternCount(s, "itag=") > 1)
        {
            int i = s.indexOf("itag=");
            int j = s.indexOf("&", i + 1);
            String s1 = s.substring(0, i);
            String s2 = s.substring(j);
            s = (new StringBuilder(String.valueOf(s1))).append(s2).toString();
        }
        return s;
    }

	public static SpannableString txt_underline(String txt){
		SpannableString content = new SpannableString(txt);
		content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
		return content;
	}

	public static void setTextViewColorPartial(TextView view, String fulltext, String subtext, int color) {
		try{
			view.setText(fulltext, TextView.BufferType.SPANNABLE);
			Spannable str = (Spannable) view.getText();
			int i = fulltext.indexOf(subtext);
			str.setSpan(new ForegroundColorSpan(color), i, i + subtext.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		}catch (IndexOutOfBoundsException e) {
		}
	}

	/**
	 * 이메일 포맷 체크
	 * @param email
	 * @return
	 */

	public final static boolean isValidEmail(CharSequence target) {
		if (TextUtils.isEmpty(target)) {
			return false;
		} else {
			return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
		}
	}

	/*************************************************************
	 * 숫자값에 3글자 단위로 콤마 처리
	 *
	 * @param num
	 *            원본 데이터
	 * @return String
	 *************************************************************/
	public static String getNumberFormat(int num) {
		DecimalFormat df = new DecimalFormat("#,###");
		return df.format(num).toString();
	}
}