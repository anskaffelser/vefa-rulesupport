package no.difi.vefa.rulesupport;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;

public class PagesTest {

    @Test
    public void simple() throws IOException, URISyntaxException {
        Pages pages = new Pages(Paths.get(getClass().getResource("/.index").toURI()).getParent());
        pages.load("sch/NOGOV-UBL-T10.sch");
        pages.load("sch/NONAT-UBL-T10.sch");
        pages.values().forEach(System.out::println);

        Assert.assertEquals(pages.size(), 66);
        Assert.assertNotNull(pages.get("NOGOV-T10-R001").getContent());
    }
}
