package ley.anvil.modpacktools.commands;

import j2html.TagCreator;
import j2html.tags.ContainerTag;
import ley.anvil.modpacktools.command.CommandReturn;
import ley.anvil.modpacktools.command.ICommand;
import ley.anvil.modpacktools.command.LoadCommand;
import ley.anvil.modpacktools.commandhelper.ModInfo;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import static j2html.TagCreator.*;

@LoadCommand("createmodlist")
public class CreateModList implements ICommand {
    @Override
    public CommandReturn execute(String[] args) {
        if(args.length >= 3) {
            if(args[1].equalsIgnoreCase("csv")) {
                File csvFile = new File(args[2]);
                if(csvFile.exists()) {
                    return CommandReturn.fail("Delete " + csvFile);
                }
                System.out.println("Printing CSV into " + csvFile);
                try(CSVPrinter printer = new CSVPrinter(new FileWriter(csvFile), CSVFormat.EXCEL.withDelimiter(';'))) {
                    printer.printRecord("Name", "Authors", "Link", "Downloads", "ID");
                    printer.println();
                    ArrayList<ModInfo> modlist = ModInfo.getModInfo();
                    Collections.sort(modlist, Comparator.comparing(a -> a.getName().toLowerCase()));
                    for(ModInfo mod : modlist) {
                        String name = mod.getName();
                        String[] authorArr = mod.getAuthors();
                        String link = mod.getLink();
                        int downloads = mod.getDownloads();
                        int id = mod.getId();
                        StringBuilder sb = new StringBuilder();
                        for(String author : authorArr) {
                            sb.append(author);
                            sb.append(", ");
                        }
                        String authors = sb.toString();
                        authors = authors.substring(0, authors.length() - 2);

                        printer.printRecord(name, authors, link, downloads, id);
                    }
                }catch(IOException e) {
                    e.printStackTrace();
                }
            }else if(args[1].equalsIgnoreCase("html")) {
                File htmlFile = new File(args[2]);
                if(htmlFile.exists()) {
                    return CommandReturn.fail("Delete " + htmlFile);
                }
                ArrayList<ModInfo> mods = ModInfo.getModInfo();
                Collections.sort(mods, Comparator.comparing(a -> a.getName().toLowerCase()));
                ContainerTag table = body(
                        TagCreator.table(TagCreator.attrs("#mods"), TagCreator.tbody(
                                tr(td(b("Name")),
                                        td(b("Authors")),
                                        td(b("ID")),
                                        td(b("Downloads"))
                                ),
                                TagCreator.each(mods, i -> {
                                    StringBuilder sb = new StringBuilder();
                                    for(String author : i.getAuthors()) {
                                        sb.append(author);
                                        sb.append(", ");
                                    }
                                    String authors = sb.toString();
                                    authors = authors.substring(0, authors.length() - 2);

                                    return tr(td(a(i.getName())
                                                    .withHref(i.getLink())
                                                    .withRel("noopener noreferrer")
                                                    .withTarget("_blank")),
                                            td(authors),
                                            td(String.valueOf(i.getId())),
                                            td(String.valueOf(i.getDownloads())));
                                })
                        ))
                );
                try {
                    System.out.println("Writing HTML");
                    FileWriter htmlWrite = new FileWriter(htmlFile);
                    htmlWrite.write(table.render());
                    htmlWrite.close();
                }catch(IOException e) {
                    e.printStackTrace();
                }
            }else {
                return CommandReturn.fail("Expected Either HTML or CSV as format");
            }
        }else {
            return CommandReturn.fail("Syntax: createmodlist <csv/html> <file>");
        }
        return CommandReturn.success();
    }
}
