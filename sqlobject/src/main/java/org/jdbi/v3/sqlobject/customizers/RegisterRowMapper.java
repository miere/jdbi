/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jdbi.v3.sqlobject.customizers;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;

import org.jdbi.v3.core.Query;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.sqlobject.SqlStatementCustomizer;
import org.jdbi.v3.sqlobject.SqlStatementCustomizerFactory;
import org.jdbi.v3.sqlobject.SqlStatementCustomizingAnnotation;

/**
 * Used to register a row mapper with either a sql object type or for a specific method.
 */
@SqlStatementCustomizingAnnotation(RegisterRowMapper.Factory.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface RegisterRowMapper
{
    /**
     * The row mapper classes to register
     * @return one or more row mapper classes
     */
    Class<? extends RowMapper<?>>[] value();

    class Factory implements SqlStatementCustomizerFactory
    {
        @Override
        public SqlStatementCustomizer createForMethod(Annotation annotation, Class<?> sqlObjectType, Method method)
        {
            return create((RegisterRowMapper) annotation);
        }

        @Override
        public SqlStatementCustomizer createForType(Annotation annotation, Class<?> sqlObjectType)
        {
            return create((RegisterRowMapper) annotation);
        }

        private SqlStatementCustomizer create(RegisterRowMapper ma) {
            final RowMapper<?>[] m = new RowMapper[ma.value().length];
            try {
                Class<? extends RowMapper<?>>[] mcs = ma.value();
                for (int i = 0; i < mcs.length; i++) {
                    m[i] = mcs[i].newInstance();
                }
            }
            catch (Exception e) {
                throw new IllegalStateException("unable to create a specified row mapper", e);
            }
            return statement -> {
                if (statement instanceof Query) {
                    Query<?> q = (Query<?>) statement;
                    for (RowMapper<?> mapper : m) {
                        q.registerRowMapper(mapper);
                    }
                }
            };
        }
    }
}
