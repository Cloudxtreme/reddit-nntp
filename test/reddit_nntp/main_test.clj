(ns reddit-nntp.main-test
  (:require [clojure.test :refer :all]
            [reddit-nntp.main :refer :all]))

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
