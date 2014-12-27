(ns reddit-nntp.main
  (:require [clj-http.client :as client]
            [clojure.data.json :as json]))

(defn extract-posts [posts-repr]
  (into #{}
  (map (fn [child] (get child "data"))
       (get-in posts-repr ["data" "children"]))))

(defn grab-posts-from-reddit []
  (let [opts {:client-params { "http.useragent" "reddit-nntp" }}
        resp (client/get "http://www.reddit.com/r/crypto/.json" opts)]
    (get resp :body)))

(defn pretty-print-post [post]
  (select-keys post ["title" "id"]))

(defn -main
  [& args]
  (println (-> (grab-posts-from-reddit)
               json/read-str
               extract-posts
               ((partial map pretty-print-post)))))

