package jentus.parser.importer.service;

import jentus.parser.common.model.FormJson;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FormServiceImpl implements FormService {


    public FormJson convert(FormJson formJson) {
        return formJson;
    }
}
