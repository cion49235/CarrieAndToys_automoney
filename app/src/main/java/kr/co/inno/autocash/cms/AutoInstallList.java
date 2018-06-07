package kr.co.inno.autocash.cms;
import java.util.List;

/**
 * Created by Lee Dong Su on 2017-08-27.
 */

public class AutoInstallList{
    public String code;
    public String msg;
    public int count;
    public List<AutoInstallListItem> list;

    @Override
    public String toString() {
        return "BaseModel{" +
                "code='" + code + '\'' +
                ", msg='" + msg + '\'' +
                '}';
    }
}
