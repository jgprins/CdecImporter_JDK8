package gov.water.cdec.importer.controllers;

import java.io.IOException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 *
 * @author J.G. "Koos" Prins, D.Eng. PE.
 */
@Controller()
@RequestMapping("/")
public class IndexController {
  // <editor-fold defaultstate="collapsed" desc="Constructor">
  /**
   * Public Constructor
   */
  public IndexController() {
    super();
  }
  // </editor-fold>

  @RequestMapping()
  public String indexPage() throws IOException {
    return "index";
  }
}
