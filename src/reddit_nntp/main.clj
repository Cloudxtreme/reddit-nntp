(ns reddit-nntp.main
  (:require [clj-http.client :as client]
            [clojure.data.json :as json]))

(defn extract-posts [posts-repr]
  (into #{}
  (map #(get % "data")
       (get-in posts-repr ["data" "children"]))))

(defn grab-posts-from-reddit []
  (let [opts {:client-params { "http.useragent" "reddit-nntp" }}
        resp (client/get "http://www.reddit.com/r/crypto/.json" opts)]
    (get resp :body)))

(defn grab-comments-from-reddit [post]
  [{ "id" "foo"
    "body" "This."
    "children" [ { "id" "foo.bar"
                   "body" "Worst comment"
                   "children" [] }]}])

(defn augment-post-with-comments [comments, post]
  (assoc post "children" comments))

(defn flatten-post-and-comments [post-and-comments]
  (let [has-children #(contains? % "children")
        get-children #(get % "children")
        nodes (tree-seq has-children get-children post-and-comments)
        useful-keys #(select-keys % ["id" "body" "title"])]
    (map useful-keys nodes)))

(defn reddit-nntp [grab-posts-from-reddit grab-comments-from-reddit]
  (-> (grab-posts-from-reddit)
               json/read-str
               extract-posts
               ((partial map (fn [post]
                               (augment-post-with-comments (grab-comments-from-reddit post) post))))
               ((partial map flatten-post-and-comments))))


(defn -main
  [& args]
  (println (reddit-nntp grab-posts-from-reddit grab-comments-from-reddit)))
  
