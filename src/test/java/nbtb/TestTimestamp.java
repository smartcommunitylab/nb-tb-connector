package nbtb;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.junit.Test;

public class TestTimestamp {
	
	@Test
	public void testTimeZoneConversion() throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd'T'HHmmss");
		sdf.setTimeZone(TimeZone.getTimeZone("Europe/London"));
		String timestamp = "20190129T150033";
		System.out.println(timestamp);
		Date date = sdf.parse(timestamp);
		System.out.println(date);
	}
}
