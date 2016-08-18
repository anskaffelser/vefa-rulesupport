package no.difi.vefa.rulesupport;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class Cli {

    public static void main(String... args) throws IOException, CmdLineException {
        new Cli().perform(args);
    }

    @Option(name = "--source")
    private String source = ".";
    @Option(name = "--target")
    private String target = "rulesupport";
    @Option(name = "--title")
    private String title = "Rulesupport";

    @Argument(required = true)
    private List<String> arguments = new ArrayList<>();

    private void perform(String... args) throws IOException, CmdLineException {
        CmdLineParser parser = new CmdLineParser(this);
        parser.parseArgument(args);

        Path sourcePath = Paths.get(source);

        Pages pages = new Pages(sourcePath);
        arguments.forEach(pages::load);

        Path targetPath = Paths.get(target);
        if (!Files.exists(targetPath))
            Files.createDirectories(targetPath);

        MustacheFactory mf = new DefaultMustacheFactory();
        Mustache mustachePage = mf.compile("templates/page.mustache");
        Mustache mustacheList = mf.compile("templates/list.mustache");

        Map<String, Object> values = new HashMap<>();
        values.put("rules", pages.values().stream().sorted(Comparator.comparing(Page::getIdentifier)).collect(Collectors.toList()));
        values.put("title", title);
        values.put("pages", pages);

        try (Writer writer = Files.newBufferedWriter(targetPath.resolve("index.html"))) {
            mustacheList.execute(writer, values).flush();
        }

        for (Page page : pages.values()) {
            Files.createDirectories(targetPath.resolve(page.getIdentifier()));
            Path pagePath = targetPath.resolve(String.format("%s/index.html", page.getIdentifier()));

            try (Writer writer = Files.newBufferedWriter(pagePath)) {
                values.put("page", page);
                mustachePage.execute(writer, values).flush();
            }
        }
    }
}
