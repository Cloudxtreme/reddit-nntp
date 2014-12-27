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

(defn stub-grab-posts-from-reddit []
  (json/write-str
   { "data"
     { "children"
       [
        { "data"
          { "id" "2qha"
            "title" "Foo"}}
        { "data"
          { "id" "3ab4"
            "title" "Bar"}}]}}))

(defn pretty-print-post [post]
  (select-keys post ["title" "id" "children"]))

(defn grab-comments-from-reddit [post]
  { "id" "foo"
    "body" "This."
    "children" [ "id" "foo.bar"
                 "body" "Worst comment"
                 "children" [] ]})

(defn augment-post-with-comments [comments, post]
  (assoc post "children" comments))

(defn reddit-nntp [grab-posts-from-reddit grab-comments-from-reddit]
  (-> (grab-posts-from-reddit)
               json/read-str
               extract-posts
               ((partial map (fn [post]
                               (augment-post-with-comments (grab-comments-from-reddit post) post))))
               ((partial map pretty-print-post))))


(defn -main
  [& args]
  (println (reddit-nntp grab-posts-from-reddit grab-comments-from-reddit)))
  
