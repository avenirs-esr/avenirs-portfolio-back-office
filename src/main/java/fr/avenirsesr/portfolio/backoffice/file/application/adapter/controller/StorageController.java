package fr.avenirsesr.portfolio.backoffice.file.application.adapter.controller;

import fr.avenirsesr.portfolio.common.file.application.adapter.controller.AbstractStorageController;
import fr.avenirsesr.portfolio.common.file.domain.port.input.FileResourceService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("back-office/storage")
public class StorageController extends AbstractStorageController {
  public StorageController(FileResourceService fileResourceService) {
    super(fileResourceService);
  }
}
