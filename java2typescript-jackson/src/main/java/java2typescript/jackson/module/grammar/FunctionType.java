/*******************************************************************************
 * Copyright 2013 Raphael Jolivet
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package java2typescript.jackson.module.grammar;

import java2typescript.jackson.module.grammar.base.AbstractType;

import java.io.IOException;
import java.io.Writer;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

public class FunctionType extends AbstractType {

    private LinkedHashMap<String, AbstractType> parameters = new LinkedHashMap<String, AbstractType>();

    private AbstractType resultType;

    /**
     * By default, printed as lambda function type (with =>)
     */
    @Override
    public void write(Writer writer, String moduleGeneratorName) throws IOException {
        write(writer, moduleGeneratorName, true);
    }

    /**
     * Write as non lambda : func(a:string) : string
     */
    public void writeNonLambda(Writer writer, String moduleGeneratorName) throws IOException {
        write(writer, moduleGeneratorName, false);
    }

    private void write(Writer writer, String moduleGeneratorName, boolean lambdaSyntax) throws IOException {
        writer.write("(");
        int i = 1;
        for (Entry<String, AbstractType> entry : parameters.entrySet()) {
            writer.write(entry.getKey());
            writer.write(": ");
            entry.getValue().write(writer, moduleGeneratorName);
            if (i < parameters.size()) {
                writer.write(", ");
            }
            i++;
        }
        writer.write(")" + (lambdaSyntax ? "=> " : ": "));
        resultType.write(writer, moduleGeneratorName);
    }

    public LinkedHashMap<String, AbstractType> getParameters() {
        return parameters;
    }

    public AbstractType getResultType() {
        return resultType;
    }

    public void setResultType(AbstractType resultType) {
        this.resultType = resultType;
    }

}
