package com.blade.jdbc.model;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

/**
 * 分页对象
 * 
 * @param <T>
 */
public class Paginator<T> implements Serializable {

	private static final long serialVersionUID = 9109911468475843712L;
	
	// 对象记录结果集
	private List<T> list;

	// 总记录数
	private long total = 0L;

	// 每页显示记录数
	private int limit = 10;
	
	// 总页数
	private int pages = 1;
	
	// 当前页
	private int pageNum = 1;
	
	// 下一页
	private int nextPage = 1;
	
	// 上一页
	private int prevPage = 1;
	
	//是否为第一页
	private boolean isFirstPage = false;
	
	//是否为最后一页
	private boolean isLastPage = false;
	
	//是否有前一页
	private boolean hasPrevPage = false;
	
	//是否有下一页
	private boolean hasNextPage = false;
	
	//导航页码数
	private int navPages = 8;
	
	//所有导航页号
	private int[] navPageNums;
	
	public Paginator(long total, int pageNum){
		init(total, pageNum, this.limit);
	}
	
	public Paginator(long total, int pageNum, int limit){
		init(total, pageNum, limit);
	}
	
	private void init(long total, int pageNum, int limit){
		//设置基本参数
		this.total = total;
		this.limit = limit;
		this.pages = (int) ((this.total - 1) / this.limit + 1);
		
		//根据输入可能错误的当前号码进行自动纠正
		if(pageNum < 1){
			this.pageNum = 1;
		} else if(pageNum>this.pages){
			this.pageNum = this.pages;
		} else{
			this.pageNum = pageNum;
		}
		
		//基本参数设定之后进行导航页面的计算
		this.calcNavigatePageNumbers();
		
		//以及页面边界的判定
		judgePageBoudary();
		
	}
	
	private void calcNavigatePageNumbers() {
		//当总页数小于或等于导航页码数时
		if(pages <= navPages){
			navPageNums = new int[pages];
			for(int i=0; i<pages; i++){
				navPageNums[i] = i+1;
			}
		} else{
			//当总页数大于导航页码数时
			navPageNums = new int[navPages];
			int startNum = pageNum - navPages / 2;
			int endNum = pageNum + navPages / 2;
			if(startNum < 1){
				// 最前navPageCount页
				for(int i=0; i<navPages; i++){
					navPageNums[i] = startNum++;
				}
			} else if(endNum > pages){
				endNum = pages;
				//最后navPageCount页
				for(int i=navPages-1;i>=0;i--){
					navPageNums[i]=endNum--;
				}
			} else{
				//所有中间页
				for(int i=0;i<navPages;i++){
					navPageNums[i]=startNum++;
				}
			}
		}
	}
	
	private void judgePageBoudary() {
		isFirstPage = pageNum == 1;
		isLastPage = pageNum == pages && pageNum != 1;
		hasPrevPage = pageNum != 1;
		hasNextPage = pageNum != pages;
		if(hasNextPage){
			nextPage = pageNum+1;
		}
		if(hasPrevPage){
			prevPage = pageNum-1;
		}
	}
	
	public void setList(List<T> list) {
		this.list = list;
	}
	
	public List<T> getList() {
		return list;
	}
	
	public long getTotal() {
		return total;
	}
	
	public int getLimit() {
		return limit;
	}
	
	public int getPages() {
		return pages;
	}
	
	public int getPageNum() {
		return pageNum;
	}
	
	public int[] getNavPageNums() {
		return navPageNums;
	}
	
	public boolean isFirstPage() {
		return isFirstPage;
	}
	
	public boolean isLastPage() {
		return isLastPage;
	}
	
	public boolean isHasNextPage() {
		return hasNextPage;
	}
	
	public boolean isHasPrevPage() {
		return hasPrevPage;
	}
	
	public int getNextPage() {
		return nextPage;
	}

	public int getPrevPage() {
		return prevPage;
	}

	@Override
	public String toString() {
		return "Paginator [total=" + total + ", limit=" + limit + ", pages=" + pages + ", pageNum="
				+ pageNum + ", nextPage=" + nextPage + ", prevPage=" + prevPage + ", isFirstPage=" + isFirstPage
				+ ", isLastPage=" + isLastPage + ", hasPrevPage=" + hasPrevPage + ", hasNextPage=" + hasNextPage
				+ ", navPages=" + navPages + ", navPageNums=" + Arrays.toString(navPageNums) + "]";
	}
	
}
