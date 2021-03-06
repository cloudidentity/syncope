/*
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */
package org.syncope.identityconnectors.bundles.staticwebservice.utilities;

import java.text.DecimalFormat;
import java.text.Format;
import java.text.SimpleDateFormat;

public enum AttributeType {

    String("java.lang.String"),
    Long("java.lang.Long"),
    Double("java.lang.Double"),
    Boolean("java.lang.Boolean"),
    Character("java.lang.Character"),
    Float("java.lang.Float"),
    Integer("java.lang.Integer"),
    URI("java.net.uri"),
    File("java.io.file"),
    // Date type is not supported by identityconnectors
    Date("java.lang.String");

    final private String className;

    private Format formatter;

    AttributeType(String className) {
        this.className = className;
        this.formatter = null;
    }

    public String getClassName() {
        return className;
    }

    public Format getBasicFormatter() {
        if (formatter == null) {
            switch (this) {
                case Date:
                    this.formatter = new SimpleDateFormat();
                case Long:
                case Double:
                    this.formatter = new DecimalFormat();
            }
        }

        return formatter;
    }

    public boolean isConversionPatternNeeded() {
        return this == AttributeType.Date || this == AttributeType.Double || this == AttributeType.Long;
    }
}
