;; Copyright 2019 7bridges s.r.l.
;;
;; Licensed under the Apache License, Version 2.0 (the "License");
;; you may not use this file except in compliance with the License.
;; You may obtain a copy of the License at
;;
;; http://www.apache.org/licenses/LICENSE-2.0
;;
;; Unless required by applicable law or agreed to in writing, software
;; distributed under the License is distributed on an "AS IS" BASIS,
;; WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
;; See the License for the specific language governing permissions and
;; limitations under the License.

(ns shelob.core
  (:require
   [shelob.driver :as shd]
   [shelob.messages :as shm]
   [taoensso.timbre :as timbre]
   [taoensso.timbre.appenders.core :as appenders]))

(defn init-log [ctx]
  (timbre/merge-config!
   {:appenders {:spit (appenders/spit-appender {:fname (:log-file ctx "shelob.log")})}}))

(defn init [ctx]
  (let [init-messages (:init-messages ctx)]
    (init-log ctx)
    (shd/init-driver-pool ctx)
    (when init-messages
      (shm/process-messages init-messages))))

(defn send-message
  "Sends a single `message` to the executors."
  [ctx message]
  (shm/send-batch-messages ctx [message]))

(defn send-messages
  "Sends a collection of `messages` to the executors."
  [ctx messages]
  (->> messages
       (partition-all 500)
       (reduce
        (fn [results messages-batch]
          (into results (shm/send-batch-messages ctx messages-batch)))
        [])))

(defn stop []
  (shd/close-driver-pool @shd/driver-pool))
