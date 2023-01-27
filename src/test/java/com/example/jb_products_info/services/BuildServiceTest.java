package com.example.jb_products_info.services;

import com.example.jb_products_info.dto.BuildInfoDTO;
import com.example.jb_products_info.entities.Build;
import com.example.jb_products_info.entities.Product;
import com.example.jb_products_info.repositories.BuildRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(MockitoExtension.class)
class BuildServiceTest {

    @Mock
    BuildRepository buildRepository;

    @InjectMocks
    BuildService buildService;

    private Build build1;
    private Build build2;
    private Product product1;

    @BeforeEach
    void setUp() {
        product1 = Product.builder()
                .code("CODE1")
                .name("Test name 1")
                .builds(new ArrayList<>(Arrays.asList(build1, build2)))
                .build();

        build1 = Build.builder()
                .buildNumber("testBuildNumber1")
                .version("testVersion1")
                .releaseDate(LocalDate.of(2023, 1, 23))
                .downloadUrl("testUrl1")
                .productInfoJsonData("""
                        {
                          "name": "DataGrip",
                          "version": "2022.3.3",
                          "buildNumber": "223.8617.3",
                          "productCode": "DB",
                          "dataDirectoryName": "DataGrip2022.3",
                          "svgIconPath": "bin/datagrip.svg",
                          "productVendor": "JetBrains",
                          "launch": [
                            {
                              "os": "Linux",
                              "arch": "amd64",
                              "launcherPath": "bin/datagrip.sh",
                              "javaExecutablePath": "jbr/bin/java",
                              "vmOptionsFilePath": "bin/datagrip64.vmoptions",
                              "startupWmClass": "jetbrains-datagrip",
                              "bootClassPathJarNames": [
                                "util.jar",
                                "app.jar",
                                "3rd-party-rt.jar",
                                "platform-statistics-devkit.jar",
                                "jps-model.jar",
                                "stats.jar",
                                "protobuf.jar",
                                "external-system-rt.jar",
                                "forms_rt.jar",
                                "intellij-test-discovery.jar",
                                "groovy.jar",
                                "3rd-party-native.jar",
                                "annotations-java5.jar",
                                "async-profiler-windows.jar",
                                "async-profiler.jar",
                                "byte-buddy-agent.jar",
                                "error-prone-annotations.jar",
                                "externalProcess-rt.jar",
                                "intellij-coverage-agent-1.0.682.jar",
                                "jetbrains-annotations.jar",
                                "jsch-agent.jar",
                                "platform-objectSerializer-annotations.jar",
                                "rd.jar",
                                "util_rt.jar"
                              ],
                              "additionalJvmArguments": [
                                "-Djava.system.class.loader=com.intellij.util.lang.PathClassLoader",
                                "-Didea.vendor.name=JetBrains",
                                "-Didea.paths.selector=DataGrip2022.3",
                                "-Djna.boot.library.path=$IDE_HOME/lib/jna/amd64",
                                "-Dpty4j.preferred.native.folder=$IDE_HOME/lib/pty4j",
                                "-Djna.nosys=true",
                                "-Djna.noclasspath=true",
                                "-Didea.platform.prefix=DataGrip",
                                "--add-opens=java.base/java.io=ALL-UNNAMED",
                                "--add-opens=java.base/java.lang=ALL-UNNAMED",
                                "--add-opens=java.base/java.lang.ref=ALL-UNNAMED",
                                "--add-opens=java.base/java.lang.reflect=ALL-UNNAMED",
                                "--add-opens=java.base/java.net=ALL-UNNAMED",
                                "--add-opens=java.base/java.nio=ALL-UNNAMED",
                                "--add-opens=java.base/java.nio.charset=ALL-UNNAMED",
                                "--add-opens=java.base/java.text=ALL-UNNAMED",
                                "--add-opens=java.base/java.time=ALL-UNNAMED",
                                "--add-opens=java.base/java.util=ALL-UNNAMED",
                                "--add-opens=java.base/java.util.concurrent=ALL-UNNAMED",
                                "--add-opens=java.base/java.util.concurrent.atomic=ALL-UNNAMED",
                                "--add-opens=java.base/jdk.internal.vm=ALL-UNNAMED",
                                "--add-opens=java.base/sun.nio.ch=ALL-UNNAMED",
                                "--add-opens=java.base/sun.nio.fs=ALL-UNNAMED",
                                "--add-opens=java.base/sun.security.ssl=ALL-UNNAMED",
                                "--add-opens=java.base/sun.security.util=ALL-UNNAMED",
                                "--add-opens=java.desktop/com.sun.java.swing.plaf.gtk=ALL-UNNAMED",
                                "--add-opens=java.desktop/java.awt=ALL-UNNAMED",
                                "--add-opens=java.desktop/java.awt.dnd.peer=ALL-UNNAMED",
                                "--add-opens=java.desktop/java.awt.event=ALL-UNNAMED",
                                "--add-opens=java.desktop/java.awt.image=ALL-UNNAMED",
                                "--add-opens=java.desktop/java.awt.peer=ALL-UNNAMED",
                                "--add-opens=java.desktop/java.awt.font=ALL-UNNAMED",
                                "--add-opens=java.desktop/javax.swing=ALL-UNNAMED",
                                "--add-opens=java.desktop/javax.swing.plaf.basic=ALL-UNNAMED",
                                "--add-opens=java.desktop/javax.swing.text.html=ALL-UNNAMED",
                                "--add-opens=java.desktop/sun.awt.X11=ALL-UNNAMED",
                                "--add-opens=java.desktop/sun.awt.datatransfer=ALL-UNNAMED",
                                "--add-opens=java.desktop/sun.awt.image=ALL-UNNAMED",
                                "--add-opens=java.desktop/sun.awt=ALL-UNNAMED",
                                "--add-opens=java.desktop/sun.font=ALL-UNNAMED",
                                "--add-opens=java.desktop/sun.java2d=ALL-UNNAMED",
                                "--add-opens=java.desktop/sun.swing=ALL-UNNAMED",
                                "--add-opens=jdk.attach/sun.tools.attach=ALL-UNNAMED",
                                "--add-opens=jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED",
                                "--add-opens=jdk.internal.jvmstat/sun.jvmstat.monitor=ALL-UNNAMED",
                                "--add-opens=jdk.jdi/com.sun.tools.jdi=ALL-UNNAMED"
                              ]
                            }
                          ],
                          "bundledPlugins": [
                            "Git4Idea",
                            "Remote Development Server",
                            "XPathView",
                            "com.intellij",
                            "com.intellij.CloudConfig",
                            "com.intellij.completion.ml.ranking",
                            "com.intellij.configurationScript",
                            "com.intellij.database",
                            "com.intellij.database.ide",
                            "com.intellij.dev",
                            "com.intellij.diagram",
                            "com.intellij.platform.images",
                            "com.intellij.plugins.datagrip.solarized.colorscheme",
                            "com.intellij.plugins.vibrantink.colorscheme",
                            "com.intellij.plugins.warmneon.colorscheme",
                            "com.intellij.searcheverywhere.ml",
                            "com.intellij.settingsSync",
                            "com.jetbrains.performancePlugin",
                            "intellij.grid.core.impl",
                            "intellij.grid.impl",
                            "org.intellij.intelliLang",
                            "org.jetbrains.plugins.textmate"
                          ],
                          "modules": [
                            "com.intellij.modules.coverage",
                            "com.intellij.modules.database",
                            "com.intellij.modules.database.connectivity",
                            "com.intellij.modules.database.core",
                            "com.intellij.modules.datagrip",
                            "com.intellij.modules.duplicatesDetector",
                            "com.intellij.modules.externalSystem",
                            "com.intellij.modules.json",
                            "com.intellij.modules.lang",
                            "com.intellij.modules.microservices",
                            "com.intellij.modules.platform",
                            "com.intellij.modules.python-core-capable",
                            "com.intellij.modules.remoteServers",
                            "com.intellij.modules.sql",
                            "com.intellij.modules.ssh",
                            "com.intellij.modules.ssh.attach",
                            "com.intellij.modules.ssh.ui",
                            "com.intellij.modules.structuralsearch",
                            "com.intellij.modules.ultimate",
                            "com.intellij.modules.vcs",
                            "com.intellij.modules.xdebugger",
                            "com.intellij.modules.xml",
                            "com.intellij.platform.linux",
                            "com.intellij.platform.unix",
                            "com.intellij.platform.xwindow",
                            "intellij.cloudConfig.migrationToSettingsSync",
                            "intellij.database.diagrams",
                            "intellij.database.flameGraphs",
                            "intellij.dev.psiViewer",
                            "intellij.diagram.impl/vcs",
                            "intellij.diagram.java",
                            "intellij.diagram.properties",
                            "intellij.performanceTesting.async",
                            "intellij.performanceTesting.profilers",
                            "intellij.platform.images.copyright",
                            "intellij.profiler.asyncOne",
                            "intellij.profiler.asyncWindows",
                            "intellij.profiler.common",
                            "intellij.searchEverywhereMl.vcs",
                            "intellij.searchEverywhereMl.yaml",
                            "intellij.settingsSync.git",
                            "intellij.vcs.git/terminal"
                          ],
                          "fileExtensions": [
                            "*.ane",
                            "*.ant",
                            "*.apk",
                            "*.bigtiff",
                            "*.bmp",
                            "*.cql",
                            "*.csv",
                            "*.cvperf",
                            "*.dcx",
                            "*.ddl",
                            "*.dic",
                            "*.diff",
                            "*.doc",
                            "*.docx",
                            "*.dtd",
                            "*.ear",
                            "*.egg",
                            "*.elt",
                            "*.ent",
                            "*.fnc",
                            "*.fxml",
                            "*.gif",
                            "*.gitignore",
                            "*.har",
                            "*.hlp",
                            "*.hql",
                            "*.htm",
                            "*.html",
                            "*.icns",
                            "*.ico",
                            "*.ids",
                            "*.ignore",
                            "*.ijperf",
                            "*.iml",
                            "*.ipr",
                            "*.iws",
                            "*.jar",
                            "*.jbig2",
                            "*.jhm",
                            "*.jnlp",
                            "*.jpeg",
                            "*.jpg",
                            "*.jrxml",
                            "*.json",
                            "*.json5",
                            "*.jsonl",
                            "*.jsonlines",
                            "*.ldjson",
                            "*.mdb",
                            "*.mod",
                            "*.ndjson",
                            "*.odt",
                            "*.pam",
                            "*.patch",
                            "*.pbm",
                            "*.pck",
                            "*.pcx",
                            "*.pdf",
                            "*.pgm",
                            "*.pkb",
                            "*.pks",
                            "*.pls",
                            "*.png",
                            "*.pnm",
                            "*.ppm",
                            "*.ppt",
                            "*.pptx",
                            "*.prc",
                            "*.psd",
                            "*.redis",
                            "*.regexp",
                            "*.rgbe",
                            "*.rnc",
                            "*.rng",
                            "*.sht",
                            "*.shtm",
                            "*.shtml",
                            "*.sql",
                            "*.svg",
                            "*.swc",
                            "*.tga",
                            "*.tif",
                            "*.tiff",
                            "*.tld",
                            "*.tpb",
                            "*.tps",
                            "*.trg",
                            "*.tsv",
                            "*.uml",
                            "*.vsd",
                            "*.vw",
                            "*.war",
                            "*.wbmp",
                            "*.wsdl",
                            "*.xbm",
                            "*.xhtml",
                            "*.xls",
                            "*.xlsx",
                            "*.xml",
                            "*.xpath",
                            "*.xpath2",
                            "*.xpm",
                            "*.xsd",
                            "*.xsl",
                            "*.xslt",
                            "*.xul",
                            "*.zip",
                            "exclude"
                          ]
                        }
                        """)
                .createdDateTime(LocalDateTime.of(2023, 1, 18, 10, 20, 30))
                .updatedDateTime(LocalDateTime.of(2023, 1, 18, 10, 20, 30))
                .product(product1)
                .build();

        build2 = Build.builder()
                .buildNumber("testBuildNumber2")
                .version("testVersion2")
                .releaseDate(LocalDate.of(2023, 1, 23))
                .downloadUrl("testUrl2")
                .createdDateTime(LocalDateTime.of(2023, 1, 17, 10, 20, 30))
                .updatedDateTime(LocalDateTime.of(2023, 1, 17, 10, 20, 30))
                .product(product1)
                .build();
    }

    @Test
    void testConvertToBuildInfoDTO_productInfoPresented() {
        BuildInfoDTO buildInfo = buildService.convertToBuildInfoDTO(build1);

        assertEquals(build1.getBuildNumber(), buildInfo.getBuildNumber());
        assertNotNull(buildInfo.getProductInfo());
    }

    @Test
    void testConvertToBuildInfoDTO_productInfoIsNull() {
        BuildInfoDTO buildInfo = buildService.convertToBuildInfoDTO(build2);

        assertEquals(build2.getBuildNumber(), buildInfo.getBuildNumber());
        assertNull(buildInfo.getProductInfo());
    }
}
