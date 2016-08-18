package no.difi.vefa.rulesupport;

import com.google.common.io.ByteStreams;
import org.asciidoctor.Asciidoctor;
import org.oclc.purl.dsdl.schematron.AssertReportType;
import org.oclc.purl.dsdl.schematron.Pattern;
import org.oclc.purl.dsdl.schematron.Rule;
import org.oclc.purl.dsdl.schematron.Schema;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;

public class Pages extends HashMap<String, Page> {

    private static JAXBContext jaxbContext;
    private static Asciidoctor asciidoctor = Asciidoctor.Factory.create();

    static {
        try {
            jaxbContext = JAXBContext.newInstance(Schema.class);
        } catch (JAXBException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private Path sourcePath;

    public Pages(Path sourcePath) {
        this.sourcePath = sourcePath;
    }

    public void load(String filename) {
        try (InputStream inputStream = Files.newInputStream(sourcePath.resolve(filename))) {
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            Schema schema = unmarshaller.unmarshal(new StreamSource(inputStream), Schema.class).getValue();

            schema.getAnyOrAnyOrInclude().stream()
                    .filter(o -> o instanceof Pattern)
                    .map(o -> (Pattern) o)
                    .map(Pattern::getAnyOrAnyOrInclude)
                    .flatMap(Collection::stream)
                    .filter(o -> o instanceof Rule)
                    .map(o -> (Rule) o)
                    .forEach(rule -> rule.getAnyOrAnyOrInclude().stream()
                                    .filter(o -> o instanceof JAXBElement)
                                    .map(o -> ((JAXBElement) o).getValue())
                                    .filter(o -> o instanceof AssertReportType)
                                    .map(o -> (AssertReportType) o)
                                    .forEach(a -> {
                                        if (containsKey(a.getId())) {
                                            get(a.getId()).addAssert(a);
                                        } else {
                                            Page page = new Page(rule, a);
                                            put(a.getId(), page);

                                            Path descriptionPath = sourcePath.resolve(String.format("description/%s.adoc", a.getId())).toAbsolutePath();
                                            if (Files.exists(descriptionPath))
                                                try (InputStream descriptionInputStream = Files.newInputStream(descriptionPath)) {
                                                    page.setContent(asciidoctor.convert(new String(ByteStreams.toByteArray(descriptionInputStream)), new HashMap<>()));
                                                } catch (IOException e) {
                                                    throw new RuntimeException(e.getMessage(), e);
                                                }
                                        }
                                    })
                    );
        } catch (JAXBException | IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
