(ns reddit-nntp.main-test
  (:require [clojure.test :refer :all]
            [reddit-nntp.main :refer :all]
            [clojure.data.json :as json]))

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

(deftest test-extract-posts
  (let [expected #{ {"title" "Foo" "id" "2qha"},
                    {"title" "Bar" "id" "3ab4"}}
        input { "data" {
                         "children" [
                                       { "data"
                                         { "id" "2qha",
                                           "title" "Foo" }},
                                       { "data"
                                         { "id" "3ab4",
                                           "title" "Bar" }}]}}
        actual (extract-posts input)]
    (is (= expected actual))))

(deftest test-augment-post-with-comments
  (let [input-post { "id" "2qha"
                     "title" "Is AES broken?" }
        input-comments [ { "id" "foo"
                           "body" "No." }
                         { "id" "bar"
                           "body" "Yes" } ]
        expected { "id" "2qha"
                   "title" "Is AES broken?"
                   "children" [ { "id" "foo"
                                  "body" "No." }
                                { "id" "bar"
                                  "body" "Yes" } ] }
        actual (augment-post-with-comments input-comments input-post)]
    (is (= expected actual))))

(deftest test-reddit-nntp
  (let
      [expected
       [[{"id" "2qha" "title" "Foo"}
         {"id" "foo" "body" "This."}
         {"id" "foo.bar" "body" "Worst comment"}]
        [{"id" "3ab4" "title" "Bar"}
         {"id" "foo" "body" "This."}
         {"id" "foo.bar" "body" "Worst comment"}]]
       actual (reddit-nntp stub-grab-posts-from-reddit grab-comments-from-reddit)]
       
    (is (= expected actual))))

(deftest test-flatten-post-with-no-comments
  (let
      [input { "id" "foo" }
       expected [ { "id" "foo" } ]
       actual (flatten-post-and-comments input)]
    (is (= expected actual))))

(deftest test-flatten-post-with-non-nested-comments
  (let
      [input { "id" "foo"
               "children" [ { "id" "bar" }]}
       expected [ { "id" "foo" }
                  { "id" "bar" } ]
       actual (flatten-post-and-comments input)]
    (is (= expected actual))))

(deftest test-flatten-post-with-nested-coments
  (let
      [input { "id" "foo"
               "children" [ { "id" "bar"
                              "children" [ { "id" "baz" } ] }]}
       expected [{"id" "foo"}
                {"id" "bar"}
                {"id" "baz"}]
       actual (flatten-post-and-comments input)]
    (is (= expected actual))))
