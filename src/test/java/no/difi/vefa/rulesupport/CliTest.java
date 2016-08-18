package no.difi.vefa.rulesupport;

import org.testng.annotations.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

public class CliTest {

    @Test
    public void simple() throws Exception {
        Path sourcePath = Paths.get(getClass().getResource("/.index").toURI()).getParent();

        Cli.main("--source", sourcePath.toAbsolutePath().toString(),
                "--target", sourcePath.resolve("rulesupport").toAbsolutePath().toString(),
                "--title", "EHF Invoice 2.0",
                "sch/NOGOV-UBL-T10.sch",
                "sch/NONAT-UBL-T10.sch");
    }

}
