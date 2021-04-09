package utils;

import java.util.UUID;

public class UUIDUtils {

	public static String getID(){
		return UUID.randomUUID().toString().replaceAll("-", "");
	}
}
