/**
 * Copyright 2011-2021 Asakusa Framework Team.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.asakusafw.compiler.bulkloader.testing.io;
import java.io.IOException;

import com.asakusafw.compiler.bulkloader.testing.model.Ex2;
import com.asakusafw.runtime.io.ModelOutput;
import com.asakusafw.runtime.io.RecordEmitter;
/**
 * <code>ex2</code>をTSVなどのレコード形式で出力する。
 */
public final class Ex2Output implements ModelOutput<Ex2> {
    private final RecordEmitter emitter;
    /**
     * インスタンスを生成する。
     * @param emitter 利用するエミッター
     * @throws IllegalArgumentException 引数にnullが指定された場合
     */
    public Ex2Output(RecordEmitter emitter) {
        if(emitter == null) {
            throw new IllegalArgumentException();
        }
        this.emitter = emitter;
    }
    @Override public void write(Ex2 model) throws IOException {
        emitter.emit(model.getSidOption());
        emitter.emit(model.getValueOption());
        emitter.emit(model.getStringOption());
        emitter.endRecord();
    }
    @Override public void close() throws IOException {
        emitter.close();
    }
}