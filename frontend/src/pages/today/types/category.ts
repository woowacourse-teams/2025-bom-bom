export type CategoriesCountType = {
  totalCount: number;
  categories: CategoryCountType[];
};

type CategoryCountType = {
  category: string;
  count: number;
};
