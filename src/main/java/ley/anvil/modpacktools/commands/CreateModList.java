package ley.anvil.modpacktools.commands;

import j2html.TagCreator;
import j2html.tags.ContainerTag;
import ley.anvil.addonscript.v1.AddonscriptJSON;
import ley.anvil.modpacktools.Main;
import ley.anvil.modpacktools.command.CommandReturn;
import ley.anvil.modpacktools.command.ICommand;
import ley.anvil.modpacktools.command.LoadCommand;
import ley.anvil.modpacktools.commandhelper.ModInfo;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import static j2html.TagCreator.*;

@LoadCommand
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
                    ArrayList<AddonscriptJSON.Meta> modlist = new ArrayList<>();
                    for (AddonscriptJSON.Relation rel : Main.MPJH.getJson().getDefaultVersion().getRelations("client", false, null)) {
                        modlist.add(rel.getMeta(Main.MPJH.getJson().indexes));
                    }
                    Collections.sort(modlist, Comparator.comparing(a -> a.name.toLowerCase()));
                    for(AddonscriptJSON.Meta mod : modlist) {
                        String name = mod.name;
                        AddonscriptJSON.Contributor[] authorArr = mod.contributors.toArray(new AddonscriptJSON.Contributor[0]);
                        String link = mod.website;
                        StringBuilder sb = new StringBuilder();
                        for(AddonscriptJSON.Contributor author : authorArr) {
                            sb.append(author.name);
                            sb.append(", ");
                        }
                        String authors = sb.toString();
                        authors = authors.substring(0, authors.length() - 2);

                        printer.printRecord(name, authors, link);
                    }
                }catch(IOException e) {
                    e.printStackTrace();
                }
            }else if(args[1].equalsIgnoreCase("html")) {
                File htmlFile = new File(args[2]);
                if(htmlFile.exists()) {
                    return CommandReturn.fail("Delete " + htmlFile);
                }
                ArrayList<AddonscriptJSON.Meta> mods = new ArrayList<>();
                for (AddonscriptJSON.Relation rel : Main.MPJH.getJson().getDefaultVersion().getRelations("client", false, null)) {
                    mods.add(rel.getMeta(Main.MPJH.getJson().indexes));
                }
                Collections.sort(mods, Comparator.comparing(a -> a.name.toLowerCase()));
                ContainerTag table = body(
                        TagCreator.table(TagCreator.attrs("#mods"), TagCreator.tbody(
                                tr(td(b("Name")),
                                        td(b("Authors")),
                                        td(b("ID")),
                                        td(b("Downloads"))
                                ),
                                TagCreator.each(mods, i -> {
                                    StringBuilder sb = new StringBuilder();
                                    for(AddonscriptJSON.Contributor author : i.contributors) {
                                        sb.append(author.name);
                                        sb.append(", ");
                                    }
                                    String authors = sb.toString();
                                    authors = authors.substring(0, authors.length() - 2);

                                    return tr(td(a(i.name)
                                                    .withHref(i.website)
                                                    .withRel("noopener noreferrer")
                                                    .withTarget("_blank")),
                                            td(authors));
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

    @Nonnull
    @Override
    public String getName() {
        return "createmodlist";
    }

    @Nonnull
    @Override
    public String getHelpMessage() {
        return "This creates a modlist either as html or csv file";
    }
}
