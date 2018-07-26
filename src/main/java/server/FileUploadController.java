package server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import server.storage.FileSystemStorageService;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;
import java.util.stream.Collectors;

@Controller
public class FileUploadController {

    private final FileSystemStorageService storageService;

    @Autowired
    public FileUploadController(FileSystemStorageService storageService) {
        this.storageService = storageService;
    }

    @GetMapping("/")
    public String listUploadedFiles(Model model) throws IOException {
        model.addAttribute("files", storageService.loadAll().map(
                path -> MvcUriComponentsBuilder.fromMethodName(FileUploadController.class,
                        "serveFile", path.getFileName().toString()).build().toString())
                .collect(Collectors.toList()));

        return "uploadForm";
    }

    @GetMapping("/files/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {
        try {
            Resource file = storageService.loadAsResource(filename);
            return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"" + file.getFilename() + "\"").body(file);
        } catch (IOException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/")
    public String handleFileUpload(@RequestParam("file") MultipartFile file,
            Model model) {
        try {
            storageService.store(file);
            InputStream inputSteam = file.getInputStream();
            Scanner scanner = new Scanner(inputSteam);

            int letters = 0;
            int words = 0;
            int sentences = 0;
            int syllables = 0;
            while (scanner.hasNext()) {
                String wordyBoi = scanner.next();
                letters = 0;

                for (int i = 0; i < wordyBoi.length(); i++){
                    letters ++;
                }

                if(letters >= 5){
                    syllables += letters/2;
                }
                if (wordyBoi.endsWith(".") || wordyBoi.endsWith("!") || wordyBoi.endsWith("?")){
                    sentences ++;

                }

                words ++;
            }

            double flesh1 = 0.39 * words/sentences;
            double flesh2 = 11.8 * syllables/words;

            double flesch = flesh1 + flesh2 -15.59;

            int fleshNum = 0;
            model.addAttribute("sentences", sentences);
            model.addAttribute("syllables", syllables);
            model.addAttribute("words", words);
            model.addAttribute("flesch", flesch);

            return "word-count";
        } catch (IOException e) {

        }
        return "redirect:/";
    }
}
