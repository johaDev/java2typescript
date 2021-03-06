/*
 * Copyright (c) CompuGROUP Software GmbH,
 * This software is the confidential and proprietary information of
 * CompuGROUP Software GmbH. You shall not disclose such confidential
 * information and shall use it only in accordance with the terms of
 * the license agreement you entered into with CompuGROUP Software GmbH.
 */
package java2typescript.jackson.module.grammar.base;

import java.io.IOException;
import java.io.Writer;

abstract public class AbstractPrimitiveType extends AbstractType {

    private String token;

    public AbstractPrimitiveType(String token) {
        this.token = token;
    }

    @Override
    public void write(Writer writer, String moduleGeneratorName) throws IOException {
        writer.write(token);
    }
}
