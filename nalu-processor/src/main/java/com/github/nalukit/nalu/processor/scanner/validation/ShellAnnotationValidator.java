/*
 * Copyright (c) 2018 - Frank Hossfeld
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *  use this file except in compliance with the License. You may obtain a copy of
 *  the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *  License for the specific language governing permissions and limitations under
 *  the License.
 */
package com.github.nalukit.nalu.processor.scanner.validation;

import com.github.nalukit.nalu.processor.ProcessorException;
import com.github.nalukit.nalu.processor.ProcessorUtils;
import com.github.nalukit.nalu.processor.model.MetaModel;
import com.github.nalukit.nalu.processor.model.intern.ShellModel;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.Optional;

public class ShellAnnotationValidator {

    private ProcessorUtils processorUtils;

    private ProcessingEnvironment processingEnvironment;

    private RoundEnvironment roundEnvironment;

    private MetaModel metaModel;

    private Element shellElement;

    @SuppressWarnings("unused")
    private ShellAnnotationValidator() {
    }

    private ShellAnnotationValidator(Builder builder) {
        this.processingEnvironment = builder.processingEnvironment;
        this.roundEnvironment = builder.roundEnvironment;
        this.shellElement = builder.shellElement;
        this.metaModel = builder.metaModel;

        setUp();
    }

    public static Builder builder() {
        return new Builder();
    }

    private void setUp() {
        this.processorUtils =
            ProcessorUtils.builder()
                          .processingEnvironment(this.processingEnvironment)
                          .build();
    }

    public void validate(Element element) throws ProcessorException {
        if (element instanceof TypeElement) {
            TypeElement typeElement = (TypeElement) element;
            // annotated element has to be a class
            if (!typeElement.getKind()
                            .isClass()) {
                throw new ProcessorException("Nalu-Processor: @Shell annotation must be used with a class");
            }
            //      // check, that the typeElement implements IsApplication
            //      if (!this.processorUtils.extendsClassOrInterface(this.processingEnvironment.getTypeUtils(),
            //                                                       typeElement.asType(),
            //                                                       this.processingEnvironment.getElementUtils()
            //                                                                                 .getTypeElement(IsApplication.class.getCanonicalName())
            //                                                                                 .asType())) {
            //        throw new ProcessorException("Nalu-Processor: " +
            //                                     typeElement.getSimpleName()
            //                                                .toString() +
            //                                     ": @Shells must implement IsApplication interface");
            //      }
        } else {
            throw new ProcessorException("Nalu-Processor:" + "@Shells can only be used on a type (interface)");
        }
        //    // check the name of the shells for duplicates
        //    Shells shellsAnnotation = element.getAnnotation(Shells.class);
        //    if (!Objects.isNull(shellsAnnotation)) {
        //      List<String>compareList = new ArrayList<>();
        //      for (int i = 0; i < shellsAnnotation.value().length; i++) {
        //        Shell shell = shellsAnnotation.value()[i];
        //        if (compareList.contains(shell.name())) {
        //          throw new ProcessorException("Nalu-Processor:" + "@Shell: the name >>" + shell.name() + "<< is dunplicate! Please use another unique name!");
        //        }
        //        compareList.add(shell.name());
        //      }
        //  }
    }

    public void validateName(String name) throws ProcessorException {
        Optional<ShellModel>
            optionalShellModel =
            this.metaModel.getShells()
                          .stream()
                          .filter(m -> name.equals(m.getName()))
                          .findAny();
        if (optionalShellModel.isPresent()) {
            throw new ProcessorException("Nalu-Processor:" + "@Shell: the shell ame >>" + name + "<< is already used!");
        }
    }

    public static final class Builder {

        ProcessingEnvironment processingEnvironment;

        RoundEnvironment roundEnvironment;

        MetaModel metaModel;

        Element shellElement;

        public Builder processingEnvironment(ProcessingEnvironment processingEnvironment) {
            this.processingEnvironment = processingEnvironment;
            return this;
        }

        public Builder roundEnvironment(RoundEnvironment roundEnvironment) {
            this.roundEnvironment = roundEnvironment;
            return this;
        }

        public Builder metaModel(MetaModel metaModel) {
            this.metaModel = metaModel;
            return this;
        }

        public Builder shellElement(Element shellElement) {
            this.shellElement = shellElement;
            return this;
        }

        public ShellAnnotationValidator build() {
            return new ShellAnnotationValidator(this);
        }
    }
}
