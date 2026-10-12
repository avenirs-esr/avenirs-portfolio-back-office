package fr.avenirsesr.portfolio.backoffice.file.application.adapter.controller;

import fr.avenirsesr.portfolio.common.file.application.adapter.client.FileClient;
import fr.avenirsesr.portfolio.common.file.application.adapter.controller.AbstractFileController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("back-office/files")
public class FileController extends AbstractFileController {
  public FileController(FileClient fileClient) {
    super(fileClient);
  }
}
