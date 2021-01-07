#!/bin/bash
#
# Copyright 2011-2021 Asakusa Framework Team.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

#Finalizer起動コマンド

usage() {
	cat <<EOF
Finalizerを起動します。

起動シェルスクリプト
finalizer.sh

 順  引数                             必須/任意
 -----------------------------------------------
 1   ターゲット名                      必須
 2   バッチID                          必須
 3   ジョブフローID                    必須
 4   ジョブフロー実行ID                必須

EOF
}

if [ $# -ne 4 ]
then
    usage
    exit 1
fi

_TG_ROOT="$(cd "$(dirname "$0")/.." ; pwd)"
"$_TG_ROOT/bin/recoverer.sh" "$1" "$4"

rc=$?

exit $rc
